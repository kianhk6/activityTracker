## Overview
MyRuns is an Android application, developed in Kotlin, for tracking and analyzing physical activities. It utilizes advanced features like GPS tracking and Machine Learning for activity recognition, alongside robust UI and database management.

## User Interface (UI)
### Main Activity
- **TabLayout with Fragments**: Includes tabs like 'Start', 'History', and 'Settings'. Each tab is associated with a fragment for specific functionalities.

### Profile Activity
- **Customizable User Profiles**: Allows users to input and save personal details like name, email, phone number, gender, and major.
- **Image Selection**: Option to set a profile picture using the camera or selecting from the gallery.

### Other Activities
- **Manual Input and Display**: For manually entering exercise data and viewing logs.
- **Dynamic UI Elements**: Utilizes spinners, buttons, TextViews, EditTexts, RadioButtons, and DialogFragments for interactive and responsive design.

## Database Implementation
- **Room Database**: Manages exercise entries with fields such as activity type, duration, distance, speed, calorie, and location coordinates.
- **Data Conversion**: Implements methods to convert location lists to byte arrays for database storage and vice-versa.
- **Asynchronous Operations**: Uses threads for non-blocking database read/write operations.

## Services
### Tracking Service
- **GPS Tracking**: Captures real-time location updates during physical activities.
- **Activity Lifecycle Management**: Ensures service continuity without memory leaks, handling bindings and unbindings appropriately.

### Notification Management
- **User Feedback**: Displays notifications for ongoing tracking or activity recognition.

## GPS Integration
- **Google Maps API**: Visualizes real-time and historical GPS traces.
- **Markers and Polylines**: Marks starting, ending points, and draws paths of movement.
- **Activity Display Modes**: Switches between live location tracking and historical data display.

## Machine Learning for Activity Recognition
### Data Collection
- **Accelerometer Data**: Collects sensor data to recognize different physical activities.
- **Data Collector App**: Aids in gathering training data for the classifier.

### Feature Extraction and Classification
- **FFT Processing**: Applies Fast Fourier Transform (FFT) to accelerometer readings for feature extraction.
- **Weka Integration**: Uses the Weka library for building and deploying the activity classifier.
- **Real-time Inference**: Continuously predicts current activity based on sensor data.

### UI Integration
- **Automatic Mode**: In the 'Start' tab, the app infers and displays activity types like walking or running based on the classifier's output.

## Installation Instructions
- Download and install the APK on an Android device.
- Ensure location and camera permissions are granted.

## Usage
- Navigate through tabs for different functionalities.
- Fill out the profile, start tracking activities, and review history as desired.

## Dependencies
- Android SDK, Kotlin Standard Library, Google Maps API, Weka library for ML.

## Configuration
- Set up Google Maps API key in the manifest.
- Configure Room Database and Weka classifier settings as per requirements.
