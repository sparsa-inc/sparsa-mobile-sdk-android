# Sparsa SDK - Android

The Sparsa SDK for Android provides a native interface for managing digital identities, credentials, devices, and authentication flows on the Sparsa platform.


## Requirements

- Android SDK 31+
- Kotlin 1.9.24+
- Android Studio or IntelliJ IDEA

## Installation

### GitHub Packages (Recommended)

Add the GitHub Packages Maven repository and the SDK dependency to your project:

1. In your project-level `settings.gradle.kts`, add the repository:
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven {
               url = uri("https://maven.pkg.github.com/sparsa-inc/sparsa-mobile-sdk-android")
               credentials {
                   username = providers.gradleProperty("gpr.user").orNull
                       ?: System.getenv("GITHUB_USERNAME")
                   password = providers.gradleProperty("gpr.token").orNull
                       ?: System.getenv("GITHUB_TOKEN")
               }
           }
       }
   }
   ```

2. Add your GitHub credentials to `~/.gradle/gradle.properties`:
   ```properties
   gpr.user=YOUR_GITHUB_USERNAME
   gpr.token=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
   ```
   The token needs the `read:packages` scope.

3. Add the dependency in your app's `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation("com.sparsainc.sdk:sparsa-android:1.1.1")
   }
   ```

All transitive dependencies are resolved automatically.

### Manual Installation (AAR)

Alternatively, download the latest AAR from the [Releases](https://github.com/sparsa-inc/sparsa-mobile-sdk-android/releases) page.

1. Download `sparsa-mobile-sdk.aar` from the latest release
2. Add the AAR to your project's `libs` folder
3. Add the dependency in your app's `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(files("libs/sparsa-mobile-sdk.aar"))
   }
   ```

## Quick Start

### 1. Import the SDK

```kotlin
import com.sparsainc.sdk.sparsa.Sparsa
```

### 2. Configure

Before using any SDK functionality, configure it with your tenant credentials:

```kotlin
Sparsa.configure(
    activity = activity,
    url = "https://api.<environment>.sparsainc.com",
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    onDelete = {
        // Handle device removal from digital address
    }
)
```

### 3. Import a Digital Address

```kotlin
val auth = Sparsa.importDigitalAddress(attributesJson)
println(auth.digitalAddress)
```

## API Overview

All methods are available as both `suspend` and callback variants.

### Configuration

| Method | Description |
|--------|-------------|
| `configure(activity, url, clientId, clientSecret, onDelete)` | Initialize the SDK with tenant credentials. |

### Digital Address

| Method | Description |
|--------|-------------|
| `importDigitalAddress(attributes)` | Import an existing digital address onto this device. |
| `recoverDigitalAddress(attributes)` | Recover a digital address via the recovery flow. |
| `updateDigitalAddress(digitalAddress)` | Update the current digital address. |
| `getDigitalAddress()` | Retrieve the current digital address. |

### Credentials

| Method | Description |
|--------|-------------|
| `getCredentials()` | Fetch all credentials. |
| `getCredentials(statuses, types)` | Fetch credentials filtered by status and type. |
| `getCredentialDetails(identifier)` | Get full details of a specific credential. |
| `proofProcess(attributes)` | Initiate a credential verification (proof) process. |

### Devices

| Method | Description |
|--------|-------------|
| `getDevices()` | List all devices linked to the digital address. |
| `deleteDevice(deviceIdentifier)` | Remove a device from the digital address. |
| `deviceBootstrappingVerification(onBootstrappingData)` | Link a new device via QR-based bootstrapping. |

### Push Notifications

| Method | Description |
|--------|-------------|
| `handleNotification(payload, onDelete, onError)` | Process an incoming push notification (Map). |
| `handleNotification(extras, onDelete, onError)` | Process an incoming push notification (Bundle). |
| `updateDeviceToken(token)` | Register an FCM device token. |

#### Expected Notification Payload

The SDK expects an FCM data payload with the following structure:

```json
{
  "data": {
    "notificationType": "<type>",
    "identifier": "<transaction-id>",
    "correlationId": "<correlation-id>"
  }
}
```

The payload is received as a flat `Map<String, String>` from Firebase Cloud Messaging.

#### Notification Types

| Type | `notificationType` Value | Description |
|------|--------------------------|-------------|
| Credential Verification | `CredentialVerification` | Triggers a credential verification (proof) flow. Requires `identifier` pointing to the proof request. |
| Delete Device | `DeleteDevice` | Indicates the current device was removed from the digital address. The SDK checks device status and invokes `onDelete` if the device no longer exists. |
| Information | `Information` | Generic informational notification. No SDK action is taken. |
| Test | `Test` | Test notification. No SDK action is taken. |

#### Payload Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `notificationType` | String | Yes | One of: `CredentialVerification`, `DeleteDevice`, `Information`, `Test`. |
| `identifier` | String | For `CredentialVerification` | The proof request identifier. |
| `correlationId` | String | No | Correlation ID for request tracking. |

#### Integration Example

```kotlin
// In your FirebaseMessagingService:
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Sparsa.handleNotification(
            payload = remoteMessage.data,
            onDelete = {
                // Device was removed — clear local state, navigate to setup screen
            },
            onError = { error ->
                Log.e("Notification", "Error: ${error.message}")
            }
        )
    }

    override fun onNewToken(token: String) {
        Sparsa.updateDeviceToken(token)
    }
}
```

### Localization & Recovery

| Method | Description |
|--------|-------------|
| `getLanguage()` | Get the current SDK language. |
| `setLanguage(language)` | Set the SDK language. |
| `sendRecoveryEmail(email)` | Send a recovery email. |
| `setRecoveryEmail(email)` | Set a new recovery email. |

## Documentation

Full API reference is available at:
[https://sparsa-inc.github.io/sparsa-mobile-sdk-android/sdk/com.sparsainc.sdk.sparsa/-sparsa/index.html](https://sparsa-inc.github.io/sparsa-mobile-sdk-android/sdk/com.sparsainc.sdk.sparsa/-sparsa/index.html)

## Sample App

For a complete working example, see the [sample app](./sample_app/) included in this repository.
