Project Overview
BookNest is a frontend-only mobile application for hotel booking, built using Jetpack Compose. It directly connects to a Firebase Realtime Database for all its data storage and retrieval needs, including hotel listings and booking information.

Features
Hotel Listings: Browse a list of available hotels.

Search and Filter: Search for hotels and apply filters to find specific accommodations.

Hotel Details: View detailed information about each hotel, including images, descriptions, and amenities.

Booking System: Book a hotel room by providing necessary details, with all booking data securely stored in the Firebase Realtime Database.

Prerequisites
Android Studio: The latest version is recommended.

Firebase Project: You must have a Firebase project set up with the Realtime Database enabled.

Installation and Setup
1. Clone the Repository
Start by cloning the project from GitHub:

Bash

git clone https://github.com/midankalahon786/BookNest.git
2. Open Project in Android Studio
Open the cloned directory as an Android Studio project.

3. Configure Firebase
To connect the app to your Firebase project, you need to add your google-services.json file.

In your Firebase console, go to Project settings.

Under "Your apps," select your Android app. If you haven't created one, register a new app.

Download the google-services.json file.

Copy this file into the app directory of your Android Studio project (BookNest/app/google-services.json).

Running the Application
Once Firebase is configured, sync the project with Gradle files.

Connect an Android device or use an emulator.

Click the "Run" button in Android Studio to deploy and run the app
