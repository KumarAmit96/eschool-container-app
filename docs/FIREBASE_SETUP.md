# Firebase Setup – ZBS E-SCHOOL (`com.tech.eskool`)

Step-by-step guide for creating a Firebase project and connecting it to this app
(Analytics + Cloud Messaging / push notifications).

The Gradle side is **already wired** in this repo (`google-services` plugin, Firebase BoM,
`firebase-analytics`, `firebase-messaging`, `MyFirebaseMessagingService`). For a new
Firebase project you mostly need steps 1–4 and replacing `app/google-services.json`.

---

## 1. Create the Firebase project

1. Open <https://console.firebase.google.com> and sign in with the Google account that should own the project.
2. Click **Add project** (or **Create a project**).
3. Enter a project name, e.g. `eskooltech` → **Continue**.
4. **Google Analytics**: leave enabled (the app uses `firebase-analytics`) → **Continue**.
5. Choose or create an Analytics account → **Create project**.
6. Wait for provisioning, then click **Continue** to open the project dashboard.

## 2. Register the Android app

1. On the project overview, click the **Android** icon (**Add app → Android**).
2. Fill in:
   | Field | Value |
   |---|---|
   | Android package name | `com.tech.eskool` (must match `applicationId` in `app/build.gradle`) |
   | App nickname | `ZBS E-SCHOOL` (optional) |
   | Debug signing certificate SHA-1 | see step 3 (optional for FCM, required for Google Sign-In / Phone Auth / Dynamic Links) |
3. Click **Register app**.

## 3. Add SHA-1 / SHA-256 fingerprints (recommended)

Debug keystore:

```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Release keystore (`certificate/My_school_hour.jks` – you will be prompted for the password):

```bash
keytool -list -v -keystore certificate/My_school_hour.jks
```

Or from Android Studio: **Gradle panel → app → Tasks → android → signingReport**.

Copy the **SHA-1** and **SHA-256** values and add them in
**Project settings → General → Your apps → com.tech.eskool → Add fingerprint**.

> If the app is distributed through Google Play with Play App Signing, also add the
> SHA-1/SHA-256 from **Play Console → Setup → App integrity → App signing key certificate**.

## 4. Download `google-services.json`

1. Click **Download google-services.json**.
2. Replace the existing file at:
   ```
   app/google-services.json
   ```
   (It must sit in the `app/` module folder, next to `app/build.gradle`.)
3. Check it contains `"package_name": "com.tech.eskool"` and the new `"project_id"`.
4. Click **Next** through the remaining wizard screens → **Continue to console**.

## 5. Gradle configuration (already done – verify only)

`gradle/libs.versions.toml`
```toml
[versions]
firebaseBom = "33.7.0"
googleServices = "..."

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }

[plugins]
google-service = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

Root `build.gradle`
```groovy
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.service) apply false
}
```

`app/build.gradle`
```groovy
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.service)
}

dependencies {
    implementation platform(libs.firebase.bom)
    implementation libs.firebase.analytics
    implementation libs.firebase.messaging
}
```

Then **File → Sync Project with Gradle Files**.

## 6. Cloud Messaging (push notifications)

1. In the console go to **Project settings → Cloud Messaging**.
2. Make sure **Firebase Cloud Messaging API (V1)** shows **Enabled**
   (the legacy server key API is shut down – do not use it).
3. App side (already present):
   - `AndroidManifest.xml` declares `POST_NOTIFICATIONS` and
     `com.tech.eskool.notification.MyFirebaseMessagingService` with the
     `com.google.firebase.MESSAGING_EVENT` intent filter.
   - `LoginActivity.getFCMToken(...)` fetches the device token and sends it to the backend.
   - On Android 13+ the user must grant the notification permission at runtime.

## 7. Server credentials for sending notifications

The backend sends pushes using the FCM **HTTP v1** API, which needs a service account:

1. **Project settings → Service accounts → Generate new private key** → **Generate key**.
2. A JSON file downloads – store it **on the server only**, outside the web root.
   **Never commit it to this repo or put it inside the app.**
3. The server uses it to get an OAuth2 access token and calls:
   ```
   POST https://fcm.googleapis.com/v1/projects/<PROJECT_ID>/messages:send
   Authorization: Bearer <access_token>
   Content-Type: application/json

   {
     "message": {
       "token": "<device FCM token>",
       "data": { "title": "Hello", "message": "Test notification" }
     }
   }
   ```
   (`MyFirebaseMessagingService` reads `title` and `message` from the data payload.)

## 8. Test

1. Build and run the app on a device/emulator with Google Play services.
2. Log in so the FCM token is generated (check Logcat for the token).
3. Console → **Engage → Messaging → Create your first campaign → Firebase Notification messages**
   → enter title/text → **Send test message** → paste the device token → **Test**.
4. Verify Analytics: **Analytics → DebugView** after running
   ```bash
   adb shell setprop debug.firebase.analytics.app com.tech.eskool
   ```

## 9. Troubleshooting

| Problem | Fix |
|---|---|
| `File google-services.json is missing` | File must be at `app/google-services.json`. |
| `No matching client found for package name` | Package in JSON ≠ `com.tech.eskool`; re-register app or re-download JSON. |
| No notifications on Android 13+ | Grant `POST_NOTIFICATIONS` permission in app settings. |
| Notifications only when app is open | Send `data` payload and handle it in `onMessageReceived`; check battery optimisation. |
| `401/403` from FCM v1 | Wrong service-account JSON / project ID, or FCM API (V1) disabled. |
