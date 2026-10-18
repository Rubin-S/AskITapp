# AskIT Android

Native Android app for local services and community activity, built with Jetpack Compose.

## Backend handoff

See [handoff status](docs/frontend-handoff.md), the [proposed authentication contract](docs/auth-backend-contract.md),
[current-account response contract](docs/current-account-contract.md),
and [admin-editable legal/support configuration](docs/app-config-contract.md).
The backend does not exist yet. The current app is not ready for a URL-only production handoff;
the remaining frontend work is listed explicitly so it is not transferred to the backend team.

## Client checkpoint APK

`./gradlew :app:assemblePreview` builds an installable, optimized APK at
`app/build/outputs/apk/preview/app-preview.apk`. It uses the current AskIT name and UI,
without an in-app preview notice or banner. Only this build opens Home without sign-in;
the normal debug/release entry rules are unchanged. The backend URL is forced empty,
and logout closes the checkpoint so reopening starts a fresh local session.

The preview uses the local debug signing key for direct APK sharing, not the owner's
Play upload key. It uses the application ID `com.askit.app.preview` (version name suffix
`-client-preview`), so it installs alongside a normal AskIT build instead of replacing it.

Send the [client checkpoint notes](docs/client-checkpoint.md) with the APK. Sample data,
temporary changes, incomplete backend operations, and release limitations are documented
outside the app. Never publish this preview build to Google Play.

## Modules

| Module | Role |
| --- | --- |
| `:app` | Single-activity shell (`com.askit.app`) |
| `:designsystem` | Material 3 theme and shared Compose components (navigation, create sheet, posts, stories, explore results, profile primitives) |

## Stack

Kotlin · Jetpack Compose · Material 3 · Navigation 3 · Coil · ViewModel/StateFlow · Gradle version catalog · Java 17 · minSdk 26

## Build and verify

Requires JDK 17 and the Android SDK (`local.properties` with `sdk.dir`, or `ANDROID_HOME`).

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug lintRelease
./gradlew assembleRelease bundleRelease
```

Lint runs with `warningsAsErrors`, so any new warning fails the build.

CI (`.github/workflows/ci.yml`) runs lint, unit tests and debug/release builds on every push
and pull request to `main`, and uploads lint and test reports when a step fails.

The [device validation record](docs/device-validation.md) is a September 2026 snapshot; its
test commands refer to suites that have since been removed.

`bundleRelease` produces the Android App Bundle in `app/build/outputs/bundle/release/`.
Release artifacts are unsigned by default. For an explicitly signed release, supply these
environment variables through your local secure environment or CI secret store:

- `ASKIT_UPLOAD_STORE_FILE`: path to the app owner's upload keystore (relative paths resolve from the repository root).
- `ASKIT_UPLOAD_STORE_PASSWORD`: keystore password.
- `ASKIT_UPLOAD_KEY_ALIAS`: upload-key alias.
- `ASKIT_UPLOAD_KEY_PASSWORD`: key password.

Then run `./gradlew bundleRelease -PsignRelease=true --no-configuration-cache`.
Do not put passwords in commands, tracked files, or build logs. Keep the upload keystore outside
the repository, backed up under the app owner's control. Missing signing inputs fail the build;
the flag never falls back to the debug key. A signed build still requires backend integration
and the remaining product/release checks before publishing.
