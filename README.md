# AntiSpy

AntiSpy is an Android privacy indicator app inspired by "Safe Dot". It shows a dot overlay when the camera, microphone, or location (GPS) is in use, logs usage history, and provides a user-friendly UI for privacy awareness and control.

## Features

- **Privacy Dots:** Overlay dot appears when camera, mic, or GPS is in use, with color/icon matching the sensor.
- **Sensor Usage History:** Logs and displays which app used which sensor and when, with app icon and name.
- **Permission & Tracking Controls:** User can enable/disable tracking globally or per sensor (camera, mic, GPS).
- **Foreground Notification:** Shows a persistent notification when tracking is active.
- **Robust Permission Handling:** Requests and explains all necessary permissions (accessibility, overlay, location, battery optimization, notifications).
- **Modern UI:** Built with Jetpack Compose, matching the look and feel of modern Android privacy indicators.

## Project Structure

```
app/
  src/main/java/com/masterz/antispy/
    data/                  # Room database and repository for sensor usage events
    model/                 # SensorType enum and related models
    service/               # AccessibilityListenerService for sensor detection and overlay
    ui/                    # MainActivity, Compose screens, and ViewModels
    util/                  # Utility classes (AppUtils, Preferences)
  res/                     # Layouts, drawables, and resources for overlay and UI
  AndroidManifest.xml      # Permissions and service declarations
build.gradle.kts           # Project build config
```

## Key Packages & Their Use

- **data/**: Handles persistent storage of sensor usage events using Room.
- **model/**: Contains enums and models for sensor types and event data.
- **service/**: Implements the accessibility service, overlay logic, and sensor detection.
- **ui/**: All Jetpack Compose UI, navigation, and ViewModel logic.
- **util/**: Utility functions for app info, preferences, and permission helpers.

## Kotlin Files Explained

### data/

- **SensorUsageEvent.kt**: Data class for a sensor usage event (app, sensor, timestamp, etc).
- **SensorUsageRepository.kt**: Repository for logging and retrieving sensor usage events.
- **AppDatabase.kt**: Room database setup and migration logic.

### model/

- **SensorType.kt**: Enum for CAMERA, MICROPHONE, GPS.

### service/

- **AccessibilityListenerService.kt**: Core service that detects sensor usage, manages overlay, notifications, and logs events. Respects all user toggles and permissions.

### ui/

- **MainActivity.kt**: App entry point, sets up Compose navigation, handles permission requests, and battery optimization prompt.
- **MainViewModel.kt**: ViewModel for main UI state, accessibility status, and service control.
- **HistoryScreen.kt**: Compose screen to display sensor usage history with app icons and names.
- **SettingsScreen.kt**: (If present) Compose screen for advanced settings and customization.

### util/

- **AppUtils.kt**: Utility for getting app name and icon from package name.
- **Preferences.kt**: SharedPreferences wrapper for all user toggles (tracking, camera, mic, GPS, dot color, etc).

## Permissions Used

- **Accessibility Service**: Detects sensor usage by monitoring system events.
- **Overlay Permission**: Required to display the privacy dot on top of other apps.
- **Location Permission**: Needed for passive GPS usage detection (never used to access your location).
- **Notification Permission**: For showing sensor access and foreground notifications.
- **Battery Optimization Exemption**: Ensures the service is not killed in the background.

## Important Behavior

- **Independent Toggles:** The app provides two independent toggles: one for the Accessibility Service (reflects system state, opens system settings) and one for Enable Tracking (controls app logic and notifications). These toggles do not sync or conflict with each other.
- **Sticky Notification:** The persistent notification is removed immediately when tracking is disabled, regardless of the accessibility service state.
- **No Location Access:** The app never requests or uses your actual location; it only detects when GPS hardware is active.

## Troubleshooting

- If the overlay dot or notifications do not appear/disappear as expected, ensure all permissions are granted and battery optimization is disabled for AntiSpy.

## How It Works

1. User enables tracking and grants permissions.
2. Accessibility service listens for camera, mic, and GPS usage.
3. When a sensor is used, a dot overlay appears and a notification is shown.
4. Usage is logged to the database with app info.
5. User can view history, customize settings, and control which sensors are tracked.

---
