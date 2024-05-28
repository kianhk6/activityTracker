## Overview
MyRuns is an Android application, developed in Kotlin, for tracking and analyzing physical activities. It utilizes advanced features like GPS tracking and Machine Learning for activity recognition, alongside robust UI and database management.
## Machine learning implementation
When the app is set to automatic mode, it starts tracking movement signals through the `onSensorChanged` method.

## 1. Sensor Data Collection:
The app uses the accelerometer sensor to collect data about the user's movement. This is done through the `onSensorChanged` method, which is triggered whenever the accelerometer detects a change in movement. The sensor data (acceleration values in the x, y, and z directions) is captured and the magnitude of the acceleration is computed using the formula \( m = \sqrt{x^2 + y^2 + z^2} \). These magnitudes are then added to a buffer (`mAccBuffer`) and also sent to a `sensorDataChannel`.

## 2. Data Processing with FFT:
The data collected in `sensorDataChannel` is processed in blocks. Once the block size reaches a predefined capacity (`Globals.ACCELEROMETER_BLOCK_CAPACITY`), it is processed using the Fast Fourier Transform (FFT). The FFT transforms the time-domain data (raw accelerometer data) into the frequency domain. This means it converts the signal into its constituent frequencies. After the FFT, the magnitude of each frequency component is calculated and stored in a feature vector (`inst`), along with the maximum value from the original block.

## 3. Classification with WekaClassifier:
The feature vector (`inst`) generated by the FFT is then passed to a classifier. The classifier used here is `WekaClassifier`, which is a decision tree classifier created using the Weka machine learning library. The classifier was previously trained on labeled data to recognize different activities (e.g., standing, walking, running). The `classifyActivity` method converts the feature vector into an appropriate format and uses the `WekaClassifier` to predict the activity type. The result is then used to update the `exerciseEntry.activityType`.

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
