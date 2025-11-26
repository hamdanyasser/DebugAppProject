plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.debugappproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.debugappproject"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
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

    // Animation & UI libraries
    implementation(libs.lottie)
    implementation(libs.shimmer)

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)

    // Code Execution - Janino
    implementation(libs.janino)

    // Memory Leak Detection - LeakCanary (debug only)
    debugImplementation(libs.leakcanary)

    // Google Play Billing for Subscriptions
    implementation("com.android.billingclient:billing:6.1.0")

    // Firebase (OPTIONAL - requires google-services.json)
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
