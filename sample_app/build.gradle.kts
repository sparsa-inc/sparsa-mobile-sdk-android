plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    compileSdk = 36
    namespace = "com.sparsa.dix"

    viewBinding {
        enable = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }

    defaultConfig {
        applicationId = "com.sparsa.dix"
        targetSdk = 35
        minSdk = 31
        versionCode = 2
        versionName = "1.0.0"

    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.viewmodel)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.compose)
    implementation(libs.kotlin.stdlib)
    implementation(libs.google.material)
    implementation(libs.bundles.androidx.core)
    implementation(libs.bundles.camera.app)

    implementation(libs.google.mlkit.barcode)
    implementation(libs.gson)
    implementation(libs.google.firebase.messaging)

    // SparsaMobile SDK - using local AAR
    implementation(files("libs/sparsa-mobile-sdk.aar"))
    implementation(libs.androidx.work)
}