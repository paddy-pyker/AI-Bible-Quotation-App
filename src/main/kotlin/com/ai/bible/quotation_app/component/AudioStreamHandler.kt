package com.ai.bible.quotation_app.component

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.GainProcessor
import be.tarsos.dsp.SilenceDetector
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import kotlin.math.sqrt


private val log = KotlinLogging.logger {}


@Component
class AudioStreamHandler(private val transcribeAudio: TranscribeAudio) : BinaryWebSocketHandler() {

    // A shared buffer for accumulating incoming raw audio bytes.
    private val audioBuffer = ByteArrayOutputStream()

    // Silence detector from TarsosDSP.
    // The threshold here is in decibels. Adjust as needed.
    // (A threshold of -50 dB is often a good starting point.)
    private val silenceDetector = SilenceDetector(-50.0, false)

    // Keep track of the time (in ms) of the last non-silent chunk.
    @Volatile
    private var lastSpeechTimestamp = System.currentTimeMillis()

    // If we have a silence longer than this (in milliseconds), we treat it as a break.
    private val silenceDurationThreshold = 800L  // e.g., 0.8 second

    // Map to store a separate executor for each WebSocket session
    private val sessionExecutors = mutableMapOf<WebSocketSession, ExecutorService>()


    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info { "WebSocket connection established: ${session.id}" }

        // Create a new executor for this session
        sessionExecutors[session] = Executors.newScheduledThreadPool(1)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info { "WebSocket connection closed: ${session.id}" }

        // Shut down executor only for this session
        sessionExecutors[session]?.shutdownNow()
        sessionExecutors.remove(session)
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        processAudioMessage(session, message.payload)
    }

    /**
     * Process an incoming ByteBuffer containing 32-bit float PCM samples.
     */
    fun processAudioMessage(session: WebSocketSession, messagePayload: ByteBuffer) {
        // Ensure little-endian order.
        messagePayload.order(ByteOrder.LITTLE_ENDIAN)

        // Copy the entire payload to a byte array for later buffering.
        val payloadCopy = ByteArray(messagePayload.remaining())
        messagePayload[payloadCopy]

        // Create a duplicate ByteBuffer to extract float samples without disturbing the copy.
        val floatBuffer = ByteBuffer.wrap(payloadCopy).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
        val numFloats = floatBuffer.remaining()
        val floatArray = FloatArray(numFloats)
        floatBuffer[floatArray]

        // Use TarsosDSP's SilenceDetector to decide whether this chunk is silent.
        // (Internally, the detector computes an RMS level and compares it to the threshold.)
        val audioEvent = AudioEvent(
            TarsosDSPAudioFormat(44100.0f, 32, 1, true, false)
        )
        audioEvent.floatBuffer = floatArray
        silenceDetector.process(audioEvent)


        // For our purposes, if the detector deems the chunk not silent,
        // update the last-speech timestamp.
        val isSilent = silenceDetector.isSilence(audioEvent.floatBuffer)
        if (!isSilent) {
            lastSpeechTimestamp = System.currentTimeMillis()
        }

        // Append the raw bytes (from the original payload) to the buffer.
        synchronized(audioBuffer) {
            audioBuffer.write(payloadCopy)
        }

        // Check if enough time has passed since the last non-silent chunk.
        if (System.currentTimeMillis() - lastSpeechTimestamp > silenceDurationThreshold) {
            // A pause in speech has been detected. Extract the accumulated segment.
            val segmentData = synchronized(audioBuffer) {
                val data = audioBuffer.toByteArray()
                audioBuffer.reset()
                data
            }

            // Process the segment asynchronously.
            val executor = sessionExecutors[session]
            if (executor != null && !executor.isShutdown) {
                executor.submit(ErrorHandlingRunnable {
                    processSegment(segmentData, session)
                })
            }
        }
    }

    /**
     * Process an accumulated audio segment.
     *
     * This example:
     *   1. Converts the raw 32-bit float samples into 16-bit PCM.
     *   2. Wraps the PCM data into an AudioInputStream.
     *   3. Uses Java Sound to resample the audio from 44.1 kHz to 16 kHz.
     */
    fun processSegment(segmentData: ByteArray, session: WebSocketSession) {
        // Wrap the raw data in a ByteBuffer.
        val byteBuffer = ByteBuffer.wrap(segmentData).order(ByteOrder.LITTLE_ENDIAN)
        val numFloats = byteBuffer.remaining() / 4
        val floatArray = FloatArray(numFloats)
        byteBuffer.asFloatBuffer()[floatArray]

        // --- Apply gain using TarsosDSP's GainProcessor ---
        // Create a TarsosDSP audio format for the current segment.
        val tarsosFormat = TarsosDSPAudioFormat(44100.0f, 32, 1, true, false)
        // Create an AudioEvent with the current float samples.
        val audioEvent = AudioEvent(tarsosFormat)
        audioEvent.floatBuffer = floatArray

        // Set up the GainProcessor with a desired gain factor.
        val gainValue = 1.4
        val gainProcessor = GainProcessor(gainValue)
        gainProcessor.process(audioEvent)

        // Retrieve the processed samples (the gain is applied in place)
        val processedFloats = audioEvent.floatBuffer

        // --- Check if the processed segment is loud enough ---
        val rms = calculateRMS(processedFloats)
        val rmsThreshold = 0.03  // Adjust this value based on your application needs.
        if (rms < rmsThreshold) {
            return
        }

        // Convert the 32-bit float samples (range [-1,1]) to 16-bit PCM.
        val shortArray = ShortArray(numFloats)
        for (i in floatArray.indices) {
            // Scale to 16-bit range and convert to Short.
            shortArray[i] = ((floatArray[i] * 32767).toInt()).toShort()
        }

        // Pack the 16-bit samples into a byte array (little-endian).
        val pcmByteArray = ByteArray(shortArray.size * 2)
        var index = 0
        for (sample in shortArray) {
            pcmByteArray[index++] = (sample.toInt() and 0xFF).toByte()
            pcmByteArray[index++] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }

        // Create an AudioInputStream from the PCM data using the source format (44.1 kHz, 16-bit, mono).
        val sourceFormat = AudioFormat(44100.0f, 16, 1, true, false)
        val frameSize = sourceFormat.frameSize // 2 bytes for mono 16-bit PCM
        val frameLength = pcmByteArray.size.toLong() / frameSize
        val bais = ByteArrayInputStream(pcmByteArray)
        val audioInputStream = AudioInputStream(bais, sourceFormat, frameLength)


        // Resample to the target format for Whisper (16 kHz, 16-bit, mono).
        val targetFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            16000.0f,  // target sample rate: 16 kHz
            16,        // sample size in bits
            1,         // mono
            2,         // frame size (16-bit mono = 2 bytes)
            16000.0f,  // frame rate
            false      // little-endian
        )

        transcribeAudio.transcribe(session, targetFormat, audioInputStream)
    }

    /**
     * Utility function to calculate RMS (root-mean-square) level of the float samples.
     */
    private fun calculateRMS(samples: FloatArray): Double {
        var sum = 0.0
        for (sample in samples) {
            sum += sample * sample
        }
        return sqrt(sum / samples.size)
    }

    class ErrorHandlingRunnable(private val task: () -> Unit) : Runnable {
        override fun run() {
            try {
                task()
            } catch (throwable: Throwable) {
                log.error { "Error occurred in executor service: $throwable" }
            }
        }
    }
}
