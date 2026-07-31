# Sparsa SDK - Android

The Sparsa SDK for Android provides a native interface for managing digital identities, credentials, devices, and authentication flows on the Sparsa platform.

[API Reference](https://sparsa-inc.github.io/sparsa-mobile-sdk-android)

## Requirements

- Android SDK 31+
- Kotlin 2.2+
- Android Studio or IntelliJ IDEA

## Installation

### GitHub Packages

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
       implementation("com.sparsainc.sdk:sparsa-android:1.2.1")
   }
   ```

All transitive dependencies are resolved automatically.

## Getting Started

For a visual walkthrough of how to set up your tenant, generate a client ID and secret from the console, see the [Getting Started Guide](https://sparsa-inc.github.io/sparsa-mobile-sdk-ios/documentation/sparsa/sparsamobile). The setup process is the same for both iOS and Android.

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
    url = "BASE_URL",
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

## App Signing and FIDO Registration

FIDO authentication identifies your app by its **signing certificate**, not by its
`applicationId`. At runtime the SDK derives an origin of the form:

```
android:apk-key-hash-sha256:<Base64( SHA-256( signing certificate ) )>
```

Sparsa must have that value registered before FIDO registration or authentication will
succeed. Two consequences follow, and both surprise teams:

- **Changing the signing key changes the app identity.** Debug, staging and release builds of
  the same `applicationId` are three different identities if they are signed with three
  different keys.
- **The default debug keystore is per-machine.** Android Studio generates
  `~/.android/debug.keystore` locally the first time it is needed, so every developer's debug
  build has a *different* certificate. A fingerprint registered for one laptop does nothing for
  another, and FIDO will fail on every machine except the one that was registered.

### Reading your fingerprint

For any keystore and alias:

```bash
keytool -exportcert -alias <alias> -keystore <path-to-keystore> -storepass <store-password> \
  | openssl dgst -sha256 -binary | openssl base64
```

For the default debug keystore, the alias and passwords are always the same:

```bash
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore \
  -storepass android -keypass android | openssl dgst -sha256 -binary | openssl base64
```

The resulting Base64 string should be added in FIDO2 server environment variables. Fingerprints are derived from
public certificates and are not secret — but never share the keystore file itself.

### Recommended for teams: one shared debug keystore

Rather than registering a fingerprint per developer, commit a **dedicated debug keystore** to
your project and point the `debug` build type at it. Every machine then produces the same
certificate, so a single fingerprint covers the whole team, CI included.

1. Create the keystore once and commit it (from your project root):

   ```bash
   keytool -genkeypair -v -keystore app/debug.keystore -alias yourapp-debug \
     -keyalg RSA -keysize 2048 -validity 10950 \
     -storepass android -keypass android \
     -dname "CN=YourApp Debug,O=YourOrg,C=JP"
   ```

2. Wire it into your app's `build.gradle.kts`:

   ```kotlin
   android {
       signingConfigs {
           getByName("debug") {
               storeFile = file("debug.keystore")
               storePassword = "android"
               keyAlias = "yourapp-debug"
               keyPassword = "android"
           }
       }
   }
   ```

3. Read its fingerprint with the command above and send that one value to Sparsa.

This is safe to commit: a debug keystore carries no release authority and uses the conventional
`android` password. Never reuse it for a release build, and keep it out of any `release`
`signingConfig`.

> The sample app in this repository does **not** define a `signingConfig`, so it uses each
> machine's own `~/.android/debug.keystore`. If more than one person will run it, apply the
> shared-keystore setup above to `sample_app` as well.

### Production builds

If you publish through Google Play with **Play App Signing**, Play re-signs your app, so the
certificate that reaches the device is *not* your upload key. Register the **app signing key**,
found in Play Console under *Setup → App integrity → App signing key certificate*.

That page shows SHA-256 as colon-separated hex. Convert it to the Base64 form the SDK uses:

```bash
echo "AB:CD:...:EF" | tr -d ':' | xxd -r -p | openssl base64
```

Registering the upload key instead is the most common cause of FIDO working in debug builds and
failing once the app is installed from Play.

## Documentation

Full API reference is available [here](https://sparsa-inc.github.io/sparsa-mobile-sdk-android).

## Sample App

For a complete working example, see the [sample app](./sample_app/) included in this repository.
