# Bharamputra Ecosystem Specs & Deployment Master Manual

Welcome to the **Bharamputra** premium video sharing and short-video streaming platform. This document outlines the backend architecture, API services, database structures, security configurations, and full multi-platform deployment guidelines (Android, iOS, Web, and Backend Node.js nodes).

---

## 1. Firebase Ecosystem Architectures

### 1.1 Cloud Firestore DB Schema Tables
Firestore stores user identity, channel analytics, subscriptions, videos, and custom moderation reports.

#### A. Collection: `users`
*   **Document ID**: Firebase UID
*   **Fields**:
    ```json
    {
      "id": "String (UID)",
      "email": "String",
      "name": "String",
      "handle": "String (@username)",
      "role": "String [USER | CREATOR | ADMIN]",
      "avatarUrl": "String (Storage URI)",
      "phoneNumber": "String",
      "createdAt": "Timestamp"
    }
    ```

#### B. Collection: `creator_channels`
*   **Document ID**: `chan_{UID}`
*   **Fields**:
    ```json
    {
      "id": "String (chan_UID)",
      "name": "String",
      "handle": "String",
      "avatarUrl": "String (Storage Link)",
      "bannerUrl": "String (Storage Link)",
      "bio": "String",
      "subscribers": "Integer",
      "isVerified": "Boolean",
      "videoCount": "Integer"
    }
    ```

#### C. Collection: `videos`
*   **Document ID**: Unique UUID
*   **Fields**:
    ```json
    {
      "id": "String",
      "title": "String",
      "description": "String",
      "videoUrl": "String (CDN / Storage HLS URI)",
      "thumbnailUrl": "String (Storage PNG/WEBP Link)",
      "creatorId": "String (chan_UID)",
      "creatorName": "String",
      "creatorAvatar": "String",
      "views": "Long",
      "likes": "Long",
      "uploadTime": "Timestamp",
      "duration": "String (MM:SS format)",
      "isShort": "Boolean",
      "category": "String [Cinema | Sci-Fi | Riverine | Tech | Music | Gaming | Sports]",
      "tags": "Array of Strings",
      "privacy": "String [PUBLIC | PRIVATE | UNLISTED]"
    }
    ```

#### D. Collection: `reported_content`
*   **Document ID**: Unique Report UUID
*   **Fields**:
    ```json
    {
      "id": "String",
      "contentType": "String [VIDEO | COMMENT]",
      "titleOrContent": "String (flagged text template)",
      "reportReason": "String",
      "reportedAt": "Timestamp"
    }
    ```

---

### 1.2 Access Protection Rules (Firestore Security Rules)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Core Role Verification Rule Functions
    function isAuthenticated() {
      return request.auth != null;
    }
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    function getRole() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role;
    }
    function isAdmin() {
      return isAuthenticated() && getRole() == 'ADMIN';
    }

    // Collection Security Assignments
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(userId) || isAdmin();
    }
    
    match /creator_channels/{chanId} {
      allow read: if true;
      allow write: if isAuthenticated() && (chanId == 'chan_' + request.auth.uid || isAdmin());
    }
    
    match /videos/{videoId} {
      allow read: if true; // anyone can stream public lists
      allow create, update: if isAuthenticated();
      allow delete: if isAdmin() || (resource.data.creatorId == 'chan_' + request.auth.uid);
    }
    
    match /reported_content/{repId} {
      allow create: if isAuthenticated();
      allow read, delete: if isAdmin();
    }
  }
}
```

---

## 2. API Endpoint Protocols (Standard REST Specification Node)

The microservice backend exposes secured endpoints on path `/api/*` for platform orchestration.

### 2.1 Authenticate and Secure Session
*   **Route**: `POST /api/auth/session`
*   **Headers**: `Content-Type: application/json`
*   **Request Payload**:
    ```json
    {
      "email": "user@bharamputra.com",
      "idToken": "fbase_auth_id_token_handshake"
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "message": "Session established successfully",
      "sessionToken": "bharamputra_tok_dXNlckBi...",
      "user": {
        "email": "user@bharamputra.com",
        "role": "ADMIN"
      }
    }
    ```

### 2.2 Upload Content Video
*   **Route**: `POST /api/videos/upload`
*   **Headers**: `Authorization: Bearer <sessionToken>`, `Content-Type: application/json`
*   **Request Payload**:
    ```json
    {
      "title": "Brahmaputra Rapids Expedition",
      "description": "Kayaking down the energetic currents",
      "category": "Riverine",
      "isShort": false
    }
    ```
*   **Response (210 Success)**:
    ```json
    {
      "message": "Stream registered and processed successfully into adaptive resolutions (240p-1080p)",
      "video": {
        "id": "vid_1783930104",
        "title": "Brahmaputra Rapids Expedition",
        "category": "Riverine",
        "views": 0,
        "likes": 0
      }
    }
    ```

### 2.3 Flag Content Node
*   **Route**: `POST /api/reports`
*   **Request Payload**:
    ```json
    {
      "contentId": "bbb_stream",
      "contentType": "VIDEO",
      "reason": "Copyright infringement claim"
    }
    ```
*   **Response (201 Created)**:
    ```json
    {
      "message": "Content reported successfully. Action pending.",
      "report": {
        "id": "rep_17820104",
        "contentId": "bbb_stream",
        "reason": "Copyright infringement claim"
      }
    }
    ```

### 2.4 Purge Content Node (RBAC: Admin Only)
*   **Route**: `DELETE /api/moderation/purge/:contentId`
*   **Headers**: `Authorization: Bearer <sessionToken>`
*   **Response (200 OK)**:
    ```json
    {
      "message": "Content cleared from all regional nodes."
    }
    ```

---

## 3. Platform Multi-Target Building & Deployment Guidelines

### 3.1 Android Node Deploy
The Android app is built using **Kotlin & Jetpack Compose**.
*   **Compilation Verification**: Use Gradle tasks.
    ```bash
    gradle assembleDebug
    ```
*   **Producing Signed Release AAB/APK**:
    1.  Ensure you configure standard environment variables for signing keys:
        *   `KEYSTORE_PATH`: Absolute path pointing to standard `my-upload-key.jks` upload certificate.
        *   `STORE_PASSWORD`, `KEY_PASSWORD`: Aligned passwords representing the keys.
    2.  Execute standard compile task:
        ```bash
        gradle assembleRelease
        ```
    3.  Retrieve output files from: `app/build/outputs/apk/release/`

### 3.2 iOS Node Deploy
*   **Prerequisites**: macOS, Xcode 14+, CocoaPods.
*   **Build Actions**:
    1.  Navigate into standard platforms directory: `cd ios/`
    2.  Execute standard package resolve: `pod install`
    3.  Open the workspace file in Xcode: `open Runner.xcworkspace`
    4.  Configure **Signing & Capabilities** tab by linking an active Apple Developer team.
    5.  Trigger Product Archive compilation: **Product -> Archive** to submit to App Store Connect.

### 3.3 Web Node Deploy
*   **Prerequisites**: Node.js, static file CDN indexers.
*   **Actions**:
    1.  Trigger production building of compiler bundles (Vue/React or Web compiler equivalents):
        ```bash
        npm run build
        ```
    2.  Deploy the output `/dist` static directory directory assets onto high capacity edge clouds like Cloudflare, Firebase Hosting, or AWS CloudFront CDN networks.

### 3.4 Express Backend Node Deployment (via Docker Composer)
Our REST server leverages containerized Docker files for high-availability cluster deployments.
*   **Launch Docker Cluster Local**:
    ```bash
    # Move into backend dir
    cd backend/
    
    # Build image from local Dockerfile and boot container on port 5000
    docker build -t bharamputra-api .
    docker run -p 5000:5000 -d bharamputra-api
    ```
*   **Health Check**: Route a browser page request to `http://localhost:5000/api/videos` to confirm immediate JSON grid feedback.
