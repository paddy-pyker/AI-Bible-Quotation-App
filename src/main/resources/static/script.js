// script.js

let mediaStream = null;
let socket = null;
let audioContext = null;
let mediaStreamSource = null;
let processor = null;

const startButton = document.getElementById('startButton');
const stopButton = document.getElementById('stopButton');

// WebSocket connection
function connectWebSocket() {
    socket = new WebSocket(`wss://${window.location.hostname}:8443/audioStream`);

    socket.onopen = () => {
        console.log('WebSocket connection established');
    };

    socket.onclose = () => {
        console.log('WebSocket connection closed');
    };

    socket.onerror = (error) => {
        console.error('WebSocket error:', error);
    };
}

// Start capturing and streaming audio
startButton.addEventListener('click', () => {
    navigator.mediaDevices.getUserMedia({ audio: true })
        .then((stream) => {
            mediaStream = stream;
            connectWebSocket();

            audioContext = new (window.AudioContext || window.webkitAudioContext)();
            mediaStreamSource = audioContext.createMediaStreamSource(stream);
            processor = audioContext.createScriptProcessor(2048, 1, 1);

            processor.onaudioprocess = (event) => {
                const inputBuffer = event.inputBuffer.getChannelData(0); // Raw PCM audio data

                if (socket.readyState === WebSocket.OPEN) {
                    socket.send(inputBuffer.buffer); // Send raw PCM audio to backend via WebSocket
                }
            };

            mediaStreamSource.connect(processor);
            processor.connect(audioContext.destination);

            // Disable Start button and enable Stop button
            startButton.disabled = true;
            stopButton.disabled = false;
        })
        .catch((error) => {
            console.error("Error accessing audio input:", error);
        });
});

// Stop capturing and stop streaming audio
stopButton.addEventListener('click', () => {
    if (mediaStream) {
        mediaStream.getTracks().forEach(track => track.stop());
    }

    if (socket) {
        socket.close(); // Close WebSocket connection
    }

    if (audioContext) {
        audioContext.close(); // Close AudioContext
    }

    // Disable Stop button and enable Start button
    startButton.disabled = false;
    stopButton.disabled = true;
});
