# Sparsa SDK - Sample App

A sample Android application demonstrating how to integrate and use the Sparsa SDK.

## Requirements

- Android SDK 31+
- Android Studio or IntelliJ IDEA
- Kotlin 2.2+
- A GitHub personal access token with the `read:packages` scope (the SDK is served from GitHub
  Packages — see the [main README](../README.md#github-packages))

## What you need to supply

Everything in this project is ready to build except one file, which cannot be committed:

| File | Status | Purpose |
|------|--------|---------|
| `upload-keystore.jks` | **you provide** | Signs the app. Required for FIDO — see below. |
| `keystore.properties` | you create from `keystore.properties.example` | Keystore credentials. |
| `google-services.json` | placeholder committed | Replace to test push notifications. |

## Setup

### 1. Add the SDK Dependency

Already declared in `build.gradle.kts`. The repository and credential configuration is described
in the [main README](../README.md#github-packages).

```kotlin
dependencies {
    implementation("com.sparsainc.sdk:sparsa-android:1.1.12")
}
```

### 2. WebAuthn / FIDO setup (required for authentication)

FIDO identifies your app by its **signing certificate**, not by its `applicationId`. At runtime
the SDK derives an origin of the form:

```
android:apk-key-hash-sha256:<Base64( SHA-256( signing certificate ) )>
```

Sparsa must have that value registered, or FIDO registration and authentication will fail.

Android generates its default debug keystore **per machine**, so if the project relied on it,
every developer would have a different certificate and FIDO would only work on whichever machine
was registered. To avoid that, this sample signs **both debug and release** with the same
keystore, so one fingerprint covers your whole team and CI.

1. Place your `upload-keystore.jks` in this directory (`sample_app/`).

   Don't have one yet? Create it once and share it with your team through a secrets manager —
   never through git:

   ```bash
   keytool -genkeypair -v -keystore upload-keystore.jks -alias upload \
     -keyalg RSA -keysize 2048 -validity 10950 \
     -dname "CN=YourOrg Sample,O=YourOrg,C=JP"
   ```

2. Create `keystore.properties` from the template and fill in your values:

   ```bash
   cp keystore.properties.example keystore.properties
   ```

   On CI, set `KEYSTORE_PASSWORD`, `KEYSTORE_ALIAS` and optionally `KEYSTORE`
   and `KEYSTORE_KEY_PASSWORD` as environment variables instead.

3. Read the fingerprint and send it to Sparsa to be registered:

   ```bash
   keytool -exportcert -alias upload -keystore upload-keystore.jks -storepass <password> \
     | openssl dgst -sha256 -binary | openssl base64
   ```

   The fingerprint is derived from a public certificate and is not secret. **The keystore file and
   its passwords are** — keep both out of git and out of email.

Both `upload-keystore.jks` and `keystore.properties` are gitignored. If the keystore is absent the
project still builds, falling back to the local debug keystore, and Gradle prints a warning — but
FIDO will then only work on that one machine.

> Already publishing through Google Play? Play App Signing re-signs your app, so the certificate
> that reaches the device is not your upload key. See
> [Production builds](../README.md#production-builds) in the main README.

### 3. Configure Credentials

Update the SDK configuration in `ContentViewModel.kt` with your API credentials:

```kotlin
Sparsa.configure(
    activity = activity,
    url = "<BASE_URL>",
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    onDelete = { }
)
```

### 4. Push Notifications (Optional)

`google-services.json` in this directory is a placeholder. To test push notifications, replace it
with the real file from your Firebase project. Its `package_name` must be `com.sparsa.dix`, or
change `applicationId` in `build.gradle.kts` to match your own Firebase configuration.

### 5. Build and Run

1. Connect an Android device or start an emulator
2. Click **Run** in Android Studio, or:
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

## Troubleshooting

| Symptom | Cause |
|---------|-------|
| `401 Unauthorized` resolving the SDK | GitHub token missing or lacks `read:packages`. |
| `Could not find com.sparsainc.sdk:sparsa-android:<version>` | GitHub Packages repository not added to `settings.gradle.kts`. |
| FIDO fails only on some machines | Those machines' certificates are not registered. Use one shared keystore (step 2). |
| FIDO works in debug, fails from Play | The upload key was registered instead of the Play app signing key. |
| Gradle warns `upload-keystore.jks not found` | Expected until you complete step 2. |
