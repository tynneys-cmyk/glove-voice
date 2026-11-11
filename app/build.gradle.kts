
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.glovevoice"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.glovevoice"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
