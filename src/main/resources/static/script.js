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


function showLiveListeningScreen() {
    ftuScreen.style.display = 'none';
    liveListeningScreen.style.display = 'block';
}


function showFTUScreen() {
    ftuScreen.style.display = 'block';
    liveListeningScreen.style.display = 'none';
}

// Connect WebSocket
function connectWebSocket() {
    socket = new WebSocket(`wss://${window.location.hostname}:8443/audioStream`);

    socket.onopen = () => {
        console.log('WebSocket connection established');
    };

    socket.onmessage = (event) => {
        console.log('Received transcription:', event.data);

        const parsedData = JSON.parse(event.data);

        // Extract the title and message
        const title = parsedData.title;
        const message = parsedData.message;

        // Update the DOM elements
        transcribedRef.textContent = title;
        transcribedText.textContent = message;
    };


    socket.onclose = () => {
        console.log('WebSocket connection closed');
        showFTUScreen();
        startButton.disabled = false;
        stopButton.disabled = true;
    };

    socket.onerror = (error) => {
        console.error('WebSocket error:', error);
        showFTUScreen();
        startButton.disabled = false;
        stopButton.disabled = true;
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

            showLiveListeningScreen();

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

    showFTUScreen();

    // Reset button states
    startButton.disabled = false;
    stopButton.disabled = true;
});
