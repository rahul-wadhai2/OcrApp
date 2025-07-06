# 📱 OcrApp
## Project Overview
This application, developed in Android Kotlin, capture business card photo and extracts the text using OCR technology.

# 🚀 Installation
**Step 1: Clone the Repository**

**Step 2: Open in Android Studio**
1. Launch Android Studio → File → Open → Select the cloned project folder.
2. Wait for Gradle sync to complete (download dependencies automatically).

# 🔥 Firebase Integration Guide
**Prerequisites**
* Android Studio
* Create firebase account (https://firebase.google.com/)
* Please check Google Play services on your Android device/emulator

**Step 1: Create a Firebase Project**
1. Go to Firebase Console
2. Click Add project → Enter project name → Follow setup wizard

**Step 2: Add Firebase to Your Android App**
1. In Firebase Console:
* Click Android icon
* Enter your package name (e.g.com.calluscompany.ocrapp)
* Click Register app

**Step 3: Download Config File**
1. Download google-services.json
2. Place it in your Android project app module root directory: ```/app/google-services.json```

## How It Works
![App Screen Shot](./screenshots/screenshot1.jpeg)
* In the image above, you will see the CAPTURE CARD button. Click on this button to capture the business card photo.
* The extracted text will be populated in the corresponding fields. (**Note:** If the text from the captured image is not set in any fields, please enter it manually.)
* Click the SAVE button to successfully upload the data to the Realtime Database in Firebase.
