# BHARAMPUTRA & BHARAMPUTRA STUDIO - DEPLOYMENT AND API DOCUMENTATION

This document provides complete instructions for configuring, building, and deploying **Bharamputra** (the premium streaming platform) and **Bharamputra Studio** (the creator ecosystem) across Android, iOS, and Web.

---

## 1. PROJECT ARCHITECTURE OVERVIEW

Both applications follow modern **Clean Architecture** principles using the **MVVM (Model-View-ViewModel)** design pattern.

```
├── com.example
│   ├── MainActivity.kt (Action Transit Dispatcher)
│   ├── data
│   │   ├── BharamputraDatabase.kt (Room Offline Local Mirror)
│   │   ├── auth (User Accounts & Device Permissions)
│   │   ├── dao (SQL Queries for Channels, Videos, Comments)
│   │   ├── gemini (AI Metadata Expansion Suite)
│   │   └── models (Room entities - VideoItem, CreatorChannel, VideoComment)
│   └── ui
│       ├── screens
│       │   ├── SplashScreen.kt (Modern Fluid Intro)
│       │   ├── AuthScreen.kt (Profile Switcher & OTP Validation)
│       │   ├── MainFeedScreen.kt (Infinity Stream & Video Player Hub)
│       │   ├── AdminScreen.kt (Platform Content Moderation)
│       │   └── StudioScreen.kt (Bharamputra Creator Studio Platform)
│       └── theme (Pitch-Black Sophisticated Slate Themes & Micro-ripple Accents)
```

---

## 2. FIREBASE INTEGRATION SETUP

To link the Room-based mock/preview schemas to live production backend servers:

### A. Authentication Setup
1. Open the [Firebase Console](https://console.firebase.google.com).
2. Click **Add Project** and register `com.aistudio.bharamputra`.
3. Go to **Authentication** -> **Sign-in Method** -> Enable **Email/Password** and **Phone AUTH**.
4. Download the `google-services.json` (Android) and `GoogleService-Info.plist` (iOS).

### B. Cloud Firestore Rules & Schema
Deploy the following schema configuration rules to secure user uploads:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /creator_channels/{channelId} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == channelId;
    }
    match /videos/{videoId} {
      allow read: if true;
      allow write: if request.auth != null && request.resource.data.creatorId == request.auth.uid;
      allow delete: if request.auth != null && resource.data.creatorId == request.auth.uid;
    }
    match /comments/{commentId} {
      allow read: if true;
      allow write: if request.auth != null;
      allow delete: if request.auth != null && (resource.data.userId == request.auth.uid || get(/databases/$(database)/documents/videos/$(resource.data.videoId)).data.creatorId == request.auth.uid);
    }
  }
}
```

### C. Firebase Cloud Messaging (FCM) & Push Notifications
1. Add the Firebase Messaging dependency to the build setup:
   `implementation("com.google.firebase:firebase-messaging-ktx")`
2. Create `BharamputraFCMService.kt` extending `FirebaseMessagingService` to capture creator events:
   - Milestone gains (+10% views)
   - Fan comments & pins
   - Payout notification dispatches

---

## 3. PRODUCTION SIGNING & GRADLE RELEASE REQS

To compile high-performance Release APKs / App Bundles (.aab):

### A. keystore Configuration
Generate a cryptographically secure upload key:
```bash
keytool -genkey -v -keystore release.keystore -alias bharamputra-alias -keyalg RSA -keysize 2048 -validity 10000
```

### B. Inject credentials securely (Secrets Panel)
Add values to the workspace environment config (`.env`) or project secrets:
- `RELEASE_KEY_PASSWORD`
- `RELEASE_STORE_PASSWORD`

Configure the `app/build.gradle.kts` release configuration block:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: "default_pwd"
            keyAlias = "bharamputra-alias"
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: "default_pwd"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## 4. CROSS-PLATFORM PORTING MATRIX (FLUTTER / WEB / iOS)

If you migrate the Native Kotlin/Compose codebase into a unified **Flutter** app:

1. **State Management**: Translate Compose’s `StateFlow` controls into Flutter **bloc** or **Riverpod** providers.
2. **Database Conversion**: Swap Android Room with Flutter's **Floor** or **Isar Database** for client persistence.
3. **UI Engine**: Map Room model lists directly into adaptive widgets utilizing responsive layouts like Flex/LayoutBuilder.
