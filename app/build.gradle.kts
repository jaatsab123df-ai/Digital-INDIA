plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.digitalindia"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 🚨 5 ALAG APPS KI ID AR ICONS KA JUGAAD
    flavorDimensions.add("appType")

    productFlavors {
        create("mainApp") {
            dimension = "appType"
            applicationId = "com.barak.digitalindia.main"
            manifestPlaceholders["appName"] = "Bharamputra Main"
            manifestPlaceholders["appIcon"] = "@drawable/icon_main"
        }
        create("musicApp") {
            dimension = "appType"
            applicationId = "com.barak.digitalindia.music"
            manifestPlaceholders["appName"] = "Bharamputra Music"
            manifestPlaceholders["appIcon"] = "@drawable/icon_music"
        }
        create("chatApp") {
            dimension = "appType"
            applicationId = "com.barak.digitalindia.chat"
            manifestPlaceholders["appName"] = "Bharamputra Chat"
            manifestPlaceholders["appIcon"] = "@drawable/icon_chat"
        }
        create("adminApp") {
            dimension = "appType"
            applicationId = "com.barak.digitalindia.admin"
            manifestPlaceholders["appName"] = "Bharamputra Admin"
            manifestPlaceholders["appIcon"] = "@drawable/icon_barak"
        }
        create("analyticsApp") {
            dimension = "appType"
            applicationId = "com.barak.digitalindia.analytics"
            manifestPlaceholders["appName"] = "Bharamputra Analytics"
            manifestPlaceholders["appIcon"] = "@drawable/icon_analytics"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
