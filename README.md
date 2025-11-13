# SparsaMobile SDK - Sample App

This repository contains a sample Android application demonstrating how to integrate and use the SparsaMobile SDK.

## Requirements

- Android SDK 31 or higher
- Kotlin 1.9.24 or higher
- Android Studio or IntelliJ IDEA

## Installation

### Download the AAR

Download the latest AAR from the [Releases](https://github.com/sparsa-inc/sparsa-mobile-sdk-android/releases) page.

**Two versions available:**
- **Regular AAR** (~38 MB) - Requires you to add dependencies
- **Fat AAR** (~107 MB) - All dependencies bundled (recommended for simplicity)

#### Option 1: Using Fat AAR (Recommended)

1. Download `sparsa-mobile-sdk-fat.aar` from the latest release
2. Add the AAR to your project's `libs` folder
3. Add the dependency in your app's `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(files("libs/sparsa-mobile-sdk-fat.aar"))
   }
   ```

That's it! All required dependencies are bundled inside.

#### Option 2: Using Regular AAR

1. Download `sparsa-mobile-sdk.aar` from the latest release
2. Add the AAR to your project's `libs` folder
3. Add the dependencies in your app's `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(files("libs/sparsa-mobile-sdk.aar"))

       // Required dependencies
       implementation("io.ktor:ktor-client-core:2.3.12")
       implementation("io.ktor:ktor-client-android:2.3.12")
       implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
       implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
       implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
       implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
   }
   ```

### Try the Sample App

For a complete working example, see the [sample app](./sample_app/README.md) included in this repository.

## Quick Start

1. Download and add the AAR to your project (see Installation above)
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
