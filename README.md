# AI Bible Quotation App

---

**A basic AI-powered Bible quotation app that listens to live sermons and displays Bible quotations in real-time.**

## Overview

---
**When the user taps the START button, the app streams voice to the server.
The server transcribes the audio using OpenAI Whisper (tiny model),
streams the transcription to Google Gemini Flash to extract a Bible quote address (if detected or implied),
queries a Bible database for the full quotation, and returns it for display.**

**The app is designed to be flexible and robust, allowing users to:**

* **Switch between different Bible versions, such as NIV, KJV, or NKJV, with simple voice commands (e.g., "Give me the NIV version")**


* **Handle explicit mentions of scriptures, scripture finding/quoting, and implicit mentions (e.g., "Next Verse") with ease, mimicking the experience of listening to a sermon**

**This allows users to seamlessly interact with the app and access their preferred Bible versions and scriptures in real-time.**


## Getting Started

### Step 1: Obtain a Google API Key

🔑 **Head over to [Google AI Studio](https://aistudio.google.com/apikey) to obtain a free API key.**

### Step 2: Clone the Repository

**📋 Clone the repository using the following command:**

    git clone https://github.com/paddy-pyker/AI-Bible-Quotation-App.git && cd AI-Bible-Quotation-App

### Step 3: Configure Environment Variables

**📝 Rename .env.example to .env and set GEMINI_API_KEY to your obtained API key:**

    GEMINI_API_KEY=AIzaSy1234567890abcdefgHijkLMNOPQRSTuvwXYz


### Step 4: Start the Application

**🚀 Start the application using Docker Compose:**

    docker compose up

### Step 5: Access the Application

**📊 Access the application by visiting https://{network-address}:8443 in your web browser after application initialization completes**

## Notes

---

* **Make sure to replace {network-address} with the actual network address of your machine.**
* **This application uses a self-signed certificate, which may raise security warnings in your browser.**
