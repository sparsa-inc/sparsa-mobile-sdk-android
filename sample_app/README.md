# Sparsa SDK - Sample App

A sample Android application demonstrating how to integrate and use the Sparsa SDK.

## Requirements

- Android SDK 31+
- Android Studio or IntelliJ IDEA
- Kotlin 2.2+

## Setup

### 1. Add the SDK Dependency

The SDK is available via GitHub Packages. See the [main README](../README.md) for repository configuration.

```kotlin
dependencies {
    implementation("com.sparsainc.sdk:sparsa-android:1.1.1")
}
```

### 2. Configure Credentials

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

### 3. Push Notifications (Optional)

To test push notification handling, add your `google-services.json` file to the `sample_app/` directory and configure Firebase Cloud Messaging.

### 4. Build and Run

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
