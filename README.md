# WalkTracker

A production-ready Android app for tracking walking distance with GPS, built with Kotlin and Jetpack Compose.

## Features

### Core Features
- **GPS Tracking**: High-accuracy GPS location tracking with noise filtering
- **Start/Pause/Stop Controls**: Full session management
- **Distance Tracking**: Real-time distance calculation with accuracy filtering
- **Step Counting**: Device sensor integration with distance-based fallback
- **Local History**: Persistent storage of all walking sessions
- **Offline Support**: Works without internet connection

### Advanced Features
- **Step Calibration**: Quick calibration tool for personalized step length
- **Google Maps Integration**: Optional path visualization with polylines
- **Unit Support**: Metric (km/m) and Imperial (miles/feet) units
- **Battery Optimization**: Efficient GPS tracking with batching
- **Privacy Focused**: No data collection, works entirely offline

## Technical Specifications

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM with Repository pattern
- **UI**: Jetpack Compose with Material 3
- **Database**: Room with SQLite
- **Location**: Google Play Services Location API
- **Maps**: Google Maps Compose (optional)

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26+
- Google Play Services (for location and maps)

### 1. Clone and Import
```bash
git clone <repository-url>
cd WalkTracker
```
Open the project in Android Studio.

### 2. Google Maps API Key (Optional)
To enable map functionality:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the Maps SDK for Android
4. Create an API key
5. Open `app/src/main/res/values/strings.xml`
6. Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key:

```xml
<string name="google_maps_key">YOUR_ACTUAL_API_KEY</string>
```

### 3. Build and Run

**Option A – Android Studio (recommended)**  
1. Open the project in Android Studio.  
2. Connect an Android device or start an emulator.  
3. Click **Run** (or **Build → Make Project**).  
4. Grant location permissions when prompted.

**Option B – Command line**  
Ensure Java 17 is installed and set `JAVA_HOME`, then:

```bash
./gradlew assembleDebug
```

The first run will download the Gradle distribution. Output APK: `app/build/outputs/apk/debug/app-debug.apk`.

### 4. Testing on Pixel Device
For best GPS testing experience:
1. Enable Developer Options on your Pixel
2. Enable "Mock location app" in Developer Options
3. Use apps like "GPS Test" to verify GPS accuracy
4. Test outdoors for best GPS signal

## Architecture

### Project Structure
```
app/
├── data/                    # Data layer
│   ├── AppDb.kt            # Room database
│   ├── WalkSession.kt      # Session entity
│   ├── WalkPath.kt         # Path entity
│   ├── WalkDao.kt          # Session DAO
│   ├── WalkPathDao.kt      # Path DAO
│   └── UserPrefs.kt        # User preferences
├── tracking/               # Location tracking
│   └── LocationService.kt  # Foreground GPS service
├── ui/                     # UI layer
│   ├── MainActivity.kt     # Main activity
│   ├── MainViewModel.kt    # Main view model
│   └── screens/            # Compose screens
│       ├── HomeScreen.kt
│       ├── MapScreen.kt
│       ├── CalibrationScreen.kt
│       └── SettingsScreen.kt
└── util/                   # Utilities
    ├── Formatters.kt       # Data formatting
    └── Steps.kt            # Step counting logic
```

### Key Components

#### LocationService
- Foreground service for continuous GPS tracking
- Implements accuracy filtering (≤25m accuracy required)
- Stale location filtering (≤30s age)
- Movement threshold filtering (≥1m movement)
- Battery-optimized with batching support

#### Step Counter
- Primary: Device step sensor (TYPE_STEP_COUNTER)
- Fallback: Distance-based estimation using step length
- User-configurable step length with calibration tool

#### Data Persistence
- Room database for session and path data
- DataStore for user preferences
- Encoded polylines for efficient path storage

## Privacy & Battery Notes

### Privacy
- **No Data Collection**: All data stays on device
- **No Analytics**: No tracking or analytics
- **No Ads**: Completely ad-free
- **Offline First**: Works without internet connection

### Battery Optimization
- **Efficient GPS**: 2-second intervals with 5m minimum distance
- **Batching**: Up to 10-second update delays for battery savings
- **Accuracy Filtering**: Rejects poor GPS fixes to reduce processing
- **Foreground Service**: Proper notification for background tracking

### Location Accuracy
- **High Accuracy Mode**: Uses PRIORITY_HIGH_ACCURACY
- **Filtering**: Rejects fixes with >25m accuracy
- **Movement Threshold**: Ignores movements <1m to reduce noise
- **Path Smoothing**: 3m threshold for path point addition

## Usage

### Starting a Walk
1. Tap "Start Walk" on the home screen
2. Grant location permissions if prompted
3. A notification will appear showing tracking is active
4. Walk normally - distance and steps will update in real-time

### During a Walk
- **Pause**: Tap "Pause" to temporarily stop tracking
- **Resume**: Tap "Resume" to continue tracking
- **View Map**: Tap the map icon to see your current path

### Ending a Walk
1. Tap "Stop" to end the session
2. The walk will be saved to your history
3. View details by tapping on history items

### Calibrating Step Length
1. Go to Settings → Calibration
2. Find a measured distance (e.g., 100m track)
3. Walk the distance and count your steps
4. Enter the distance and step count
5. Tap "Calculate" and "Save"

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Start/stop walk sessions
- [ ] Pause/resume functionality
- [ ] Distance tracking accuracy
- [ ] Step counting (sensor and fallback)
- [ ] History persistence
- [ ] Map path display
- [ ] Step calibration
- [ ] Unit conversion
- [ ] Offline functionality
- [ ] Permission handling

## Troubleshooting

### GPS Issues
- **Poor Accuracy**: Test outdoors with clear sky view
- **No Location Updates**: Check location permissions
- **Battery Drain**: Ensure app is not in battery optimization

### Maps Not Loading
- **Check API Key**: Verify Google Maps API key is correct
- **Internet Connection**: Maps require internet (tracking doesn't)
- **API Quotas**: Check Google Cloud Console for quota limits

### Step Counting Issues
- **No Steps**: Check if device has step sensor in Settings
- **Inaccurate Steps**: Calibrate step length using known distance
- **Sensor Not Working**: App will fall back to distance-based counting

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Google Play Services for location and maps APIs
- Jetpack Compose team for the modern UI framework
- Material Design for the design system
- Android community for best practices and examples
