# Firestore Setup & Integration Guide

This guide provides step-by-step instructions to set up the Firebase backend for **geochat** and integrate it into the Android project.

## **Part 1: Firebase Console Setup**

### **1. Create a Firebase Project**
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project**.
3. Name it **geochat** (or similar).
4. Disable Google Analytics for this project (simplifies setup), or keep it if desired.
5. Click **Create project**.

### **2. Add the Android App**
1. In the project overview, click the **Android icon** (bugdroid) to add an app.
2. **Android package name**: `info.benjaminhill.geochat` (Matches `app/build.gradle.kts`).
3. **App nickname**: geochat.
4. **Debug signing certificate SHA-1**:
   - You can get this from Android Studio or the command line.
   - **Command Line (Mac/Linux):**
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - **Command Line (Windows):**
     ```cmd
     keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
     ```
   - Copy the `SHA1` string (e.g., `DA:39:A3:EE:5E...`) and paste it into the Firebase console.
5. Click **Register app**.

### **3. Download Config File**
1. Download the `google-services.json` file.
2. Move this file into the `app/` directory of the project: `geochat/app/google-services.json`.
3. Click **Next** in the console until you finish the wizard.

---

## **Part 2: Authentication Setup**

To ensure security rules function correctly (`request.auth != null`), we will enable Anonymous Authentication.

1. In the Firebase Console, go to **Build** > **Authentication**.
2. Click **Get started**.
3. Select the **Sign-in method** tab.
4. Click **Anonymous**.
5. Toggle **Enable** and click **Save**.

---

## **Part 3: Firestore Database Setup**

### **1. Create Database**
1. In the Firebase Console, go to **Build** > **Firestore Database**.
2. Click **Create database**.
3. Select **Production mode** (we will apply our own rules).
4. Click **Next**.
5. **Location**: Select a region close to you (e.g., `nam5` (us-central), `eur3` (europe-west)).
6. Click **Enable**.

### **2. Security Rules**
1. Go to the **Rules** tab in Firestore.
2. Replace the existing rules with the following. These rules enforce the schema defined in `GEMINI.md` and ensure only authenticated users can read/write.

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // Helper function to validate schema
    function isValidPost() {
      let post = request.resource.data;
      return post.keys().hasAll(['userId', 'text', 'timestamp', 'location', 'geohash', 'plusCode'])
        && post.text is string
        && post.text.size() > 0
        && post.text.size() < 500
        && post.location is latlng
        && post.geohash is string
        && post.geohash.size() >= 1
        && post.userId == request.auth.uid; // User can only create posts with their own ID
    }

    match /posts/{postId} {
      // Allow read if user is authenticated
      allow read: if request.auth != null;

      // Allow create if user is authenticated and data is valid
      allow create: if request.auth != null && isValidPost();

      // (Optional) Allow delete if the user owns the post
      allow delete: if request.auth != null && resource.data.userId == request.auth.uid;

      // No updates allowed (posts are immutable in this design)
      allow update: if false;
    }
  }
}
```
3. Click **Publish**.

### **3. Indexes**
Firestore automatically creates single-field indexes. For Geo queries (filtering by `geohash` range and potentially sorting by other fields), specific composite indexes might be needed later.
* If the app crashes with a "Need Index" error log, simply **click the link in the Logcat error**. It will take you directly to the Firebase Console page to create the exact index needed.

---

## **Part 4: Android Project Integration (Code Changes)**

You (or the developer) need to update the dependencies to include Firebase.

### **1. Update `gradle/libs.versions.toml`**
Add the Firebase BOM and libraries to the version catalog.

```toml
[versions]
# ... existing versions ...
googleServices = "4.4.2"
firebaseBom = "33.10.0"

[libraries]
# ... existing libraries ...
google-services = { group = "com.google.gms", name = "google-services", version.ref = "googleServices" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }

[plugins]
# ... existing plugins ...
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

### **2. Update `build.gradle.kts` (Project Level)**
*Note: In modern Gradle setups, the root build file often just has an alias to plugins if using `plugins {}` block. Ensure the google-services plugin is available.*

### **3. Update `app/build.gradle.kts` (App Level)**
Apply the Google Services plugin and add dependencies.

```kotlin
plugins {
    // ... existing plugins
    alias(libs.plugins.google.services) // Add this
}

dependencies {
    // ... existing dependencies

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // GeoFire (Uncomment this)
    implementation(libs.geofire.common)
}
```

### **4. Sync Gradle**
Click **Sync Now** in Android Studio.

---

## **Part 5: Next Steps (Implementation)**

Once the environment is set up:
1.  **Dependency Injection (Hilt):** Create a `FirebaseModule` to provide `FirebaseAuth` and `FirebaseFirestore` instances.
2.  **Auth Implementation:** In `MainActivity` or a startup logic, sign in anonymously:
    ```kotlin
    FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
        Log.d("Auth", "Signed in as ${it.user?.uid}")
    }
    ```
3.  **Repository Switch:** Replace `MockPostRepository` with a real implementation that queries Firestore using `GeoFireUtils` bounds.
