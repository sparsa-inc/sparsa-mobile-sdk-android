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
            multiDexEnabled = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            multiDexEnabled = true
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"

            pickFirsts += "**"
        }
        jniLibs {
            pickFirsts += "**"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += "DuplicatePlatformClasses"
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains:annotations:23.0.0")
        force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        force("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    }
}

dependencies {
    // Sparsa Mobile SDK from JitPack
    implementation("com.github.sparsa-inc:sparsa-mobile-sdk-android:1.0.0")

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.compose.viewmodel)

    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.legacy.support)

    implementation(libs.bundles.camera.app)
    implementation(libs.androidx.camera.camera2.v132)

    implementation(libs.accompanist.themeadapter)
    implementation(libs.accompanist.permissions)

    implementation(libs.bundles.ktor)
    implementation(libs.bundles.fuel)

    implementation(libs.bundles.serialization)
    implementation(libs.jackson.kotlin)
    implementation(libs.jwt)

    implementation(libs.google.material)
    implementation(libs.google.mlkit.barcode)
    implementation(libs.google.firebase.messaging)
    implementation(libs.androidx.work)
}