# Android client-testing APK

This isolated Capacitor project bundles the existing React website. The live website build and backend schema are unchanged. Package ID: `com.apbrahmanapurohithsamakya.app`. Android 7.0+ (API 24), target Android 16 (API 36).

The bundled app uses `https://purohith-api.onrender.com/api`. Its local HTTPS WebView origin is the organization website domain, which is already allowed by the API CORS configuration. No remote `server.url`, cleartext network traffic, API credentials, or database changes are required.

## Build

Install frontend dependencies with `npm ci --prefix frontend` from the repository root. In `mobile`, run `npm ci`, then `npm run sync`. Open `mobile/android` in Android Studio with SDK 36 / Java 21, or run `./gradlew assembleDebug` there. The APK is `android/app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions builds the APK when the `feature/android-apk` branch is pushed. Download and extract the `AP-Purohit-Samakya-test-APK` artifact from the successful run. Transfer `app-debug.apk` to an Android phone and permit installation from that file source when prompted. This is a debug-signed client-testing APK, not a Play Store release. Fresh CI builds may have different debug signing keys, so an older test APK may need uninstalling before installing a later one; that clears its local login/draft token but leaves server data intact.

## Native behavior

Android Back navigates within the app or minimizes at the first page. Downloads open Android sharing so files can be saved or sent. ID-card Print opens Android printing / Save as PDF. Uploads use the Android file chooser. Audio/video pause when the app backgrounds. No broad storage permissions are requested.

Before client rollout, test login, draft resume, document/photo upload, payment reference submission, admin document previews, Excel sharing, and ID-card Save as PDF on a physical Android phone. Only the build and browser checks can run on a machine without an Android device/emulator.

The mobile-only Vite configuration injects `runtime.ts`; native integration code is not included in the production website bundle. Web changes require rebuilding the APK. Play Store publication and release signing are separate steps.
