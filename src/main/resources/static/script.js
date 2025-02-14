let mediaStream = null;
let socket = null;
let audioContext = null;
let mediaStreamSource = null;
let processor = null;

// Grabbing references to DOM elements
const ftuScreen = document.getElementById('ftuScreen');
const liveListeningScreen = document.getElementById('liveListeningScreen');

const startButton = document.getElementById('startButton');
const stopButton = document.getElementById('stopButton');

const transcribedRef = document.getElementById('transcribedReference');
const transcribedText = document.getElementById('transcribedText');

// Show the Live Listening screen, hide FTU
function showLiveListeningScreen() {
    ftuScreen.style.display = 'none';
    liveListeningScreen.style.display = 'block';
}

// Show the FTU screen, hide Live Listening
function showFTUScreen() {
    ftuScreen.style.display = 'block';
    liveListeningScreen.style.display = 'none';
}

// Connect WebSocket
function connectWebSocket() {
    // For your environment, might be wss://192.168.8.143:8443 or similar
    socket = new WebSocket(`wss://${window.location.hostname}:8443/audioStream`);

    socket.onopen = () => {
        console.log('WebSocket connection established');
    };

    // When the server sends a transcript, replace the placeholder text
    socket.onmessage = (event) => {
        console.log('Received transcription:', event.data);
        // Replace the placeholder with the actual text
        // If you only have a single line of text, you can put it all in transcribedText
        transcribedRef.textContent = 'Detected Verse';
        transcribedText.textContent = event.data;
    };

    socket.onclose = () => {
        console.log('WebSocket connection closed');
    };

    socket.onerror = (error) => {
        console.error('WebSocket error:', error);
    };
}

// Start capturing audio
startButton.addEventListener('click', () => {
    navigator.mediaDevices.getUserMedia({ audio: true })
        .then((stream) => {
            mediaStream = stream;
            connectWebSocket();

            audioContext = new (window.AudioContext || window.webkitAudioContext)();
            mediaStreamSource = audioContext.createMediaStreamSource(stream);
            processor = audioContext.createScriptProcessor(2048, 1, 1);

            processor.onaudioprocess = (event) => {
                if (socket && socket.readyState === WebSocket.OPEN) {
                    const inputBuffer = event.inputBuffer.getChannelData(0); // Float32Array
                    socket.send(inputBuffer.buffer); // Send raw PCM audio data
                }
            };

            mediaStreamSource.connect(processor);
            processor.connect(audioContext.destination);

            // Switch screens
            showLiveListeningScreen();

            // Update button states
            startButton.disabled = true;
            stopButton.disabled = false;
        })
        .catch((error) => {
            console.error("Error accessing audio input:", error);
            alert("Microphone access denied or error occurred.");
        });
});

// Stop capturing audio
stopButton.addEventListener('click', () => {
    if (mediaStream) {
        mediaStream.getTracks().forEach(track => track.stop());
    }

    if (socket) {
        socket.close();
    }

    if (audioContext) {
        audioContext.close();
    }

    // Return to FTU screen
    showFTUScreen();

    // Reset button states
    startButton.disabled = false;
    stopButton.disabled = true;
});
