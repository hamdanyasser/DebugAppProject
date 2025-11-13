plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.debugappproject"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.debugappproject"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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