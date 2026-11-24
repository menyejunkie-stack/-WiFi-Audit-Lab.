plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.wifiauditlab"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.wifiauditlab"
        minSdk = 23
        targetSdk = 29
        versionCode = 1
        versionName = "1.0"
    }
}
