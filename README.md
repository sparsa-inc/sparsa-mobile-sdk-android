# SparsaMobile SDK - Sample App

This repository contains a sample Android application demonstrating how to integrate and use the SparsaMobile SDK.

## Requirements

- Android SDK 31 or higher
- Kotlin 1.9.24 or higher
- Android Studio or IntelliJ IDEA

## Installation

Add JitPack repository and the SDK dependency to your project:

### Step 1: Add JitPack Repository

In your `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add the Dependency

In your app's `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.github.sparsa-inc:sparsa-mobile-sdk-android:1.0.0")
}
```

That's it! All transitive dependencies are resolved automatically.

### Try the Sample App

For a complete working example, see the [sample app](./sample_app/) included in this repository.

## Quick Start

1. Add the SDK dependency (see Installation above)
2. Configure the SDK in your activity:
   ```kotlin
   SparsaMobile.configure(
       activity = activity,
       url = "BASE_URL",
       clientId = "your-client-id",
       clientSecret = "your-client-secret"
   )
   ```

## Features

- Secure user authentication
- Digital identity management
- Credential verification
- Device management
- Biometric authentication support

## Documentation

For detailed documentation on how to use the SparsaMobile SDK, please refer to the [official documentation](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/documentation).

### Visual Documentation

For visual demonstrations, screenshots, and video tutorials of the SDK features, please refer to the [iOS SDK documentation](https://sparsa-inc.github.io/sparsa-mobile-sdk-ios/documentation) which includes images and videos showing the SDK in action.

## License

This SDK is proprietary software. Please contact the vendor for licensing information.
