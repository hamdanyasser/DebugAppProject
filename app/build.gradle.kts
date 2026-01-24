import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.example.debugappproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.debugappproject"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
            // Removed applicationIdSuffix to match Firebase package name
            // applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Android UI
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

    // Gson
    implementation(libs.gson)

    // WorkManager
    implementation("androidx.work:work-runtime:2.9.0")

    // Animation UI
    implementation(libs.lottie)
    implementation(libs.shimmer)
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")

    // Hilt DI
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)

    // Janino (code execution)
    implementation(libs.janino)

    // LeakCanary (debug only)
    debugImplementation(libs.leakcanary)

    // Google Play Billing
    implementation("com.android.billingclient:billing:6.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")  // Realtime Database for multiplayer

    // OkHttp for HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Markwon for Markdown rendering
    implementation("io.noties.markwon:core:4.6.2")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.janino)  // Needed for CodeExecutionEngine tests
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
