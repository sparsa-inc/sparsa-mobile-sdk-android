import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

val keystoreFile: File = file(System.getenv("KEYSTORE") ?: "upload-keystore.jks")

val keystoreProperties: Properties? = file("keystore.properties")
    .takeIf { it.exists() }
    ?.let { f -> Properties().apply { f.inputStream().use(::load) } }

fun signingValue(property: String, environmentVariable: String): String? =
    keystoreProperties?.getProperty(property) ?: System.getenv(environmentVariable)

val hasSigningKeystore: Boolean = keystoreFile.exists()

android {
    namespace = "com.sparsa.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sparsa.dix"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (hasSigningKeystore) {
            create("sparsa") {
                storeFile = keystoreFile
                storePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "KEYSTORE_ALIAS")
                keyPassword = signingValue("keyPassword", "KEYSTORE_KEY_PASSWORD")
                    ?: signingValue("storePassword", "KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            signingConfigs.findByName("sparsa")?.let { signingConfig = it }
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("sparsa")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    // Sparsa SDK
    implementation("com.sparsainc.sdk:sparsa-android:1.2.1")

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.compose.viewmodel)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)

    // Camera & QR
    implementation(libs.bundles.camera.app)
    implementation(libs.google.mlkit.barcode)
    implementation("com.google.zxing:core:3.5.4")

    // Google
    implementation(libs.google.material)
    implementation(libs.google.firebase.messaging)

    // Serialization
    implementation(libs.gson)
}
