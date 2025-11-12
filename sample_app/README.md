# SparsaMobile Sample App

This is a sample Android application demonstrating how to integrate and use the SparsaMobile SDK with manual AAR integration.

## Prerequisites

- Android SDK 31 or higher
- Android Studio or IntelliJ IDEA with the latest versions
- Kotlin 1.9.24 or higher

## Setup Instructions

### 1. Download the AAR

Download the latest version of `sparsa-mobile-sdk.aar` from the [GitHub Releases](https://github.com/sparsa-inc/sparsa-mobile-sdk-android/releases) page.

1. Go to the Releases page
2. Download `sparsa-mobile-sdk.aar` from the latest release
3. Copy the AAR file

### 2. Add AAR to the Project

1. Open the project in Android Studio
2. Navigate to `sample_app/libs/` folder (create it if it doesn't exist)
3. Paste the `sparsa-mobile-sdk.aar` file into the `libs` folder
4. Sync the project with Gradle files

### 3. Configure Dependencies

The `build.gradle.kts` should already include the AAR dependency:

```kotlin
dependencies {
    implementation(files("libs/sparsa-mobile-sdk.aar"))
    // ... other dependencies
}
```

### 4. Build and Run

1. Connect your Android device or start an emulator
2. Click "Run" in Android Studio or use:
   ```bash
   ./gradlew assembleDebug
   ```
3. Install and run the app

## Project Structure

```
sample_app/
├── src/
│   └── main/
│       ├── java/com/sparsa/sample/  # Sample app code
│       ├── res/                      # Resources
│       └── AndroidManifest.xml       # App manifest
└── build.gradle.kts                  # Build configuration
```

## Configuration

Before running the app, you'll need to configure the SDK with your API credentials. Update the configuration in your app initialization code:

```kotlin
SparsaMobile.configure(
    activity = activity,
    url = "BASE_URL",
    clientId = "your-client-id",
    clientSecret = "your-client-secret"
)
```

## Required Permissions

The sample app requires the following permissions (already included in `AndroidManifest.xml`):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Features Demonstrated

- SDK initialization and configuration
- User authentication flows
- Credential management
- Identity verification
- QR code scanning

## Troubleshooting

### Build Errors

If you encounter build errors after adding the AAR:

1. Clean the project: `Build > Clean Project`
2. Rebuild: `Build > Rebuild Project`
3. Invalidate caches and restart: `File > Invalidate Caches / Restart`

### AAR Not Found

If you see "AAR not found" errors:

1. Verify the AAR file exists in `sample_app/libs/`
2. Check that the file name matches in `build.gradle.kts`
3. Sync Gradle files

## Documentation

For detailed API documentation, visit the [official documentation](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/documentation).

## Support

For issues and questions, please refer to the main [SparsaMobile SDK repository](https://github.com/sparsa-inc/sparsa-mobile-sdk-android).
