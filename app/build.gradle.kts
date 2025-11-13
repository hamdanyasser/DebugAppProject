plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.debugappproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.debugappproject"
        minSdk = 26  // Android 8.0 - Covers 95%+ of devices
        targetSdk = 34  // Android 14 - Latest stable

        // Version management
        // Increment versionCode for each release (1, 2, 3, ...)
        // Update versionName with semantic versioning (1.0.0, 1.0.1, 1.1.0, ...)
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Enable code shrinking, obfuscation, and optimization
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Optional: Configure signing here or use signing.properties
            // signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    // Optional: Add signing configuration (keep keystore details in local file)
    // signingConfigs {
    //     create("release") {
    //         // These values should come from local.properties or gradle.properties
    //         // DO NOT commit keystore files or passwords to git
    //         storeFile = file(System.getenv("KEYSTORE_FILE") ?: "release-keystore.jks")
    //         storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
    //         keyAlias = System.getenv("KEY_ALIAS") ?: ""
    //         keyPassword = System.getenv("KEY_PASSWORD") ?: ""
    //     }
    // }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // ViewModel & LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // RecyclerView
    implementation(libs.recyclerview)

    // Gson for JSON parsing
    implementation(libs.gson)

    // WorkManager for notifications
    implementation("androidx.work:work-runtime:2.9.0")

    // Firebase (OPTIONAL - requires google-services.json)
    // TODO: Add google-services.json to app/ directory to enable Firebase
    // TODO: Add apply plugin: 'com.google.gms.google-services' to the bottom of this file
    // TODO: Configure Firebase project in console and download google-services.json
    // Firebase BOM for version management
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}