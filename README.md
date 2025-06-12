# Module Sparsa Mobile SDK for Android

## Overview
Mobile SDK contains a set of tools and methods to build platform-specific mobile applications and implement new features in existing mobile apps. The resources contained in an SDK include libraries, documents, code samples, guides, and APIs. For SDK Developer API refer [here](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/).

## Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) or [IntelliJ IDEA](https://www.jetbrains.com/idea/) with the latest versions
- [Android SDK 31](https://developer.android.com/tools/releases/platforms) or higher
- [Kotlin 1.9.24](https://kotlinlang.org/) or higher

### Installation
Add the Sparsa Mobile SDK to your project by including it in your app's `build.gradle` file:

```kotlin
dependencies {
    implementation("com.sparsa:mobile-sdk:latest.version")
}
```

### App Permissions
The following user permissions are required:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
```

### Basic Usage
Here's a simple example of how to configure the SDK:

```kotlin
// Initialize the SDK
SparsaMobile.configure(
    activity = activity,
    url = "BASE_URL",
    clientId = "client_id_generated_in_tenant_console",
    clientSecret = "client_secret_generated_in_tenant_console"
)
```

Where:
- **activity** (required): Main AppCompatActivity of the host application.
- **url** (required): This is the endpoint URL for all API calls.
- **clientId** (required): Client ID generated in tenant console.
- **clientSecret** (required): Secret generated in your tenant console.

### Core Functionality

The SDK provides several key methods for user authentication and identity verification:

There are two options, suspending function and with completion blocks

1. **User Authentication**:
   ```kotlin
   suspend fun authenticateUser(attributes: String): UserAuthenticationModel
   fun authenticateUser(attributes: String, completion: (Result<UserAuthenticationModel>)
   ```
   Imports a digital address using the QR code or AppLink sent with a recovery email.

3. **User Registration**:
   ```kotlin
   suspend fun registerUser(attributes: String): UserAuthenticationModel
   fun registerUser(attributes: String, completion: (Result<UserAuthenticationModel>)
   ```
   Imports digital address using a QR code or AppLink.



For [all functions](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/-sparsa-mobile/main/-sparsa-mobile/index.html) refer here.

## Documentation
For detailed documentation, please refer to the specific package documentation:
- [data.error](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/-sparsa-mobile/data.error/index.html) - Error handling classes
- [data.model.external](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/-sparsa-mobile/data.model.external/index.html) - External data models
- [main](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/-sparsa-mobile/main/index.html) - Main SDK components
