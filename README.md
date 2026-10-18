# AskIT Android

AskIT is a local-services and community app: people post tasks they need done, providers list
their services, and neighbours share posts and stories. This repository is the native Android
client, written in Kotlin with Jetpack Compose and Material 3. English and Tamil are shipped.

The backend does not exist yet. The app runs on local, in-memory data; see
[Backend handoff](#backend-handoff) for what the server team needs.

## Architecture at a glance

- **Single activity, Compose UI.** `MainActivity` hosts a Navigation 3 back stack
  (`AskITNav.kt`, `AskITNavEntries.kt`). Screens are `Route` composables that collect a
  ViewModel's `StateFlow` and pass plain state and callbacks to stateless `Screen` composables.
- **Hand-rolled DI.** `AskITApplication` owns an `AppContainer` (build config, variant content,
  preview storage). Account data lives in a `SessionGraph`, an activity-retained ViewModel, so it
  is dropped on sign-out. `AccountScope<T>` keeps per-account stores (jobs, inbox) from leaking
  between accounts.
- **Variant seam.** Production code depends on `VariantContent`; `AppVariant` is implemented once
  with demo fixtures (`src/demo`, debug and preview only) and once empty (`src/release`), so a
  release build cannot compile against sample data.

More detail, with a data-flow diagram: [docs/architecture.md](docs/architecture.md).

## Modules

| Module | Role |
| --- | --- |
| `:app` | The app (`com.askit.app`): navigation, features (home, stories, explore, post a task, list a service, jobs, inbox, profile, dashboards), DI, variant content. |
| `:designsystem` | Material 3 theme (`AskITTheme`, contrast helpers) and shared Compose components: navigation bar, posts and comments, stories, explore results, profile primitives, dialogs, inputs. |

Stack: Kotlin · Jetpack Compose · Material 3 · Navigation 3 · Coil · ViewModel/StateFlow ·
kotlinx.serialization · OkHttp · Gradle version catalog · JDK 17 · minSdk 26.

## Build variants

| Variant | App ID | Demo data | Use |
| --- | --- | --- | --- |
| `debug` | `com.askit.app` | yes (`src/demo`) | Local development with sample feed, people, chats, jobs and dashboards. |
| `preview` | `com.askit.app.preview` | yes, plus preview personas | Optimised client-checkpoint APK. Opens Home without sign-in, OTP `123456`, backend URL forced empty. Installs next to a normal build. Never publish it. |
| `release` | `com.askit.app` | **no** (`src/release`) | Real/local data only; screens without a backend show empty or "not available yet" states. |

Tests that need fixtures live in `app/src/testDebug`; `app/src/testRelease/ReleaseVariantTest`
fails if any fixture reaches the release classpath. Full notes: [docs/build-variants.md](docs/build-variants.md).

## Setup

1. Install JDK 17 and the Android SDK (platform 37, build-tools 37).
2. Point Gradle at the SDK with `local.properties` (`sdk.dir=/path/to/sdk`) or `ANDROID_HOME`.
3. Optional: a Google Places key in `local.properties` as `PLACES_API_KEY=...` for the search-area
   picker. `local.defaults.properties` supplies an inert placeholder so the build works without one.
4. Open the project in Android Studio, or use the Gradle wrapper below.

## Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assemblePreview            # client checkpoint APK: app/build/outputs/apk/preview/
./gradlew assembleRelease bundleRelease

# Tests
./gradlew testDebugUnitTest testReleaseUnitTest      # JVM unit tests (incl. en/ta string parity)
./gradlew connectedDebugAndroidTest                  # Compose UI tests, needs a device/emulator

# Static analysis (all fail on new findings; warningsAsErrors is on for lint)
./gradlew ktlintCheck        # ./gradlew ktlintFormat to auto-fix
./gradlew detekt
./gradlew lintDebug lintRelease
python3 scripts/check_hardcoded_strings.py           # no hardcoded user-visible strings

# Screenshot tests (Roborazzi on Robolectric)
./gradlew recordRoborazziDebug     # write baselines to */src/test*/screenshots
./gradlew verifyRoborazziDebug     # compare against committed baselines
```

Screenshot tests need Robolectric native graphics, which has no Linux arm64 build: on such hosts
they are skipped (pass `-Paskit.forceScreenshotTests` to try anyway). Record baselines on x86_64
(or macOS) and commit the PNGs.

## CI

- `.github/workflows/ci.yml` (push and pull request to `main`): hardcoded-string check, ktlint,
  detekt, Android lint, debug and release unit tests, Roborazzi verify (or record and upload a
  `roborazzi-baselines` artifact until baselines are committed), debug and release assembly.
  Reports are uploaded when a step fails.
- `.github/workflows/instrumented.yml` (pull requests, manual): emulator run of
  `connectedDebugAndroidTest` on API 28 and 34. Non-blocking.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branching, commit style, the string rules and how to
update lint baselines. Changes are listed in [CHANGELOG.md](CHANGELOG.md).

## Backend handoff

See [handoff status](docs/frontend-handoff.md), the [proposed authentication contract](docs/auth-backend-contract.md),
the [current-account response contract](docs/current-account-contract.md)
and the [admin-editable legal/support configuration](docs/app-config-contract.md).
The app is not ready for a URL-only production handoff; the remaining frontend work is listed in
the handoff doc so it is not transferred to the backend team.

Send the [client checkpoint notes](docs/client-checkpoint.md) with any preview APK. The preview is
signed with the local debug key for direct sharing, not the Play upload key.

## Release signing

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
