package com.ai.bible.quotation_app.component

import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

private val log = KotlinLogging.logger {}

@Component
class TranscribeAudio(private val gemini: Gemini) {

    private val recognizer: OfflineRecognizer = createOfflineRecognizer()
    private val sampleRate = 16_000


    fun transcribe(session: WebSocketSession, targetFormat: AudioFormat, audioInputStream: AudioInputStream) {
        val convertedAudioStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream)
        val stream = recognizer.createStream()
        stream.acceptWaveform(audioInputStreamToFloatArray(convertedAudioStream), sampleRate)
        recognizer.decode(stream)
        val text = recognizer.getResult(stream).text
        log.info { "[${session.id}] $text" }
        stream.release()

        session.sendMessage(TextMessage(parseText(text, session)))

    }

    private fun parseText(text: String, session: WebSocketSession): String {
        val geminiResponse = gemini.chat(session.id, text)
        if(geminiResponse != null && !geminiResponse.match){
            return ""
        }
//        val book = KJVBook()
//        book.title = "hello"
//        book.content = "hello world"
//
//        kjvBooksRepository.save(book)
        return geminiResponse?.title as String
    }

    private fun audioInputStreamToFloatArray(audioInputStream: AudioInputStream): FloatArray {
        val format: AudioFormat = audioInputStream.format
        val frameSize: Int = format.frameSize

        // Try to get the total number of frames from the stream
        val frameLength = audioInputStream.frameLength
        val audioBytes: ByteArray = if (frameLength != AudioSystem.NOT_SPECIFIED.toLong()) {
            // If frame length is specified, allocate the full buffer at once
            val totalBytes = (frameLength * frameSize).toInt()
            val bytes = ByteArray(totalBytes)
            var offset = 0
            while (offset < totalBytes) {
                val read = audioInputStream.read(bytes, offset, totalBytes - offset)
                if (read == -1) break
                offset += read
            }
            bytes
        } else {
            // If frame length is not specified, read until the end using a ByteArrayOutputStream
            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var read: Int
            while (audioInputStream.read(buffer).also { read = it } != -1) {
                baos.write(buffer, 0, read)
            }
            baos.toByteArray()
        }

        // Convert the byte array to a float array (assuming 16-bit little-endian samples)
        val numSamples = audioBytes.size / frameSize
        val floatArray = FloatArray(numSamples * format.channels)
        for (i in audioBytes.indices step 2) {
            // Combine two bytes into a 16-bit sample (little-endian)
            val low = audioBytes[i].toInt() and 0xFF
            val high = audioBytes[i + 1].toInt()
            val sample = (high shl 8) or low
            floatArray[i / 2] = sample / 32768.0f
        }

        return floatArray
    }

    private fun createOfflineRecognizer(): OfflineRecognizer {
        val basePath = "src/main/resources/models"

        val encoder = "$basePath/tiny.en-encoder.onnx"
        val decoder = "$basePath/tiny.en-decoder.onnx"
        val tokens = "$basePath/tiny.en-tokens.txt"

        val whisper = OfflineWhisperModelConfig.builder()
            .setEncoder(encoder)
            .setDecoder(decoder)
            .build()

        val modelConfig = OfflineModelConfig.builder()
            .setWhisper(whisper)
            .setTokens(tokens)
            .setNumThreads(Runtime.getRuntime().availableProcessors())
            .setDebug(false)
            .build()

        val config = OfflineRecognizerConfig.builder()
            .setOfflineModelConfig(modelConfig)
            .setDecodingMethod("greedy_search")
            .build()

        return OfflineRecognizer(config)
    }
}
