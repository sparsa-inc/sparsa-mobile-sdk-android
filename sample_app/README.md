# Sparsa SDK - Sample App

A sample Android application demonstrating how to integrate and use the Sparsa SDK.

## Requirements

- Android SDK 31+
- Android Studio or IntelliJ IDEA
- Kotlin 1.9.24+

## Setup

### 1. Download the AAR

Download the latest `sparsa-mobile-sdk-fat.aar` from the [Releases](https://github.com/sparsa-inc/sparsa-mobile-sdk-android/releases) page.

### 2. Add AAR to the Project

1. Place the downloaded AAR file into the `sample_app/libs/` folder
2. Sync the project with Gradle files

### 3. Configure Credentials

Update the SDK configuration in `ContentViewModel.kt` with your API credentials:

```kotlin
Sparsa.configure(
    activity = activity,
    url = "https://api.<environment>.sparsainc.com",
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    onDelete = { }
)
```

### 4. Push Notifications (Optional)

To test push notification handling, add your `google-services.json` file to the `sample_app/` directory and configure Firebase Cloud Messaging.

### 5. Build and Run

1. Connect an Android device or start an emulator
2. Click **Run** in Android Studio or use:
   ```bash
   ./gradlew assembleDebug
   ```

## Features Demonstrated

- SDK initialization and configuration
- Digital address import and recovery
- Credential management and filtering
- Credential verification (proof) process
- Device management (list, delete)
- Device bootstrapping via QR code
- Language settings
- Recovery email management
