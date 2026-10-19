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
| `demo` | `com.askit.app.demo` | yes, plus preview personas and the demo menu | Testable demo APK ("AskIT Demo"): R8-optimised, non-debuggable, signed with the committed throwaway key. Welcome screen first, **Explore demo** or any number + OTP `123456`. No backend. See [Testing the demo build](#testing-the-demo-build). |
| `release` | `com.askit.app` | **no** (`src/release`) | Real/local data only; screens without a backend show empty or "not available yet" states. |

`app/src/testRelease/ReleaseVariantTest` fails if release is a client preview or any fixture
reaches the release classpath. Full notes: [docs/build-variants.md](docs/build-variants.md).

## Testing the demo build

The `demo` APK runs every screen with no server and no real account. Everything (accounts, posts,
tasks, chats, jobs) is sample Puducherry data stored on the phone.

1. Download `AskIT-demo-*.apk` from the repo's Releases page (or `./gradlew assembleDemo`).
2. Open it on an Android 8.0+ phone and allow "Install unknown apps" for your browser or file app
   when asked. It installs as **AskIT Demo** (`com.askit.app.demo`) next to any other AskIT build.
3. Enter the app:
   - **Explore demo (no sign-up)** on the Welcome screen signs in as the seeded demo account
     (a provider with a listed service, so both seeker and provider screens work); or
   - **Get started / I already have an account**: any valid Indian mobile number (e.g.
     `98765 43210`), then OTP `123456` (shown on screen). This opens the sign-up form, so the
     sign-up flow is testable too.
4. Tap the small **DEMO** chip at the top of any screen to jump straight to any screen (Welcome,
   sign-in, sign-up, Home, Explore, Inbox/chat, profile and edit, settings, post task/service/post,
   stories, dashboards, jobs and reviews). The same menu is in **Settings → Demo build**.
5. **Settings → Demo build → Reset demo data** erases everything and closes the app (like a fresh
   install). Settings also has the preview account switcher (provider / seeker).

Handy demo values: job start code `1234`; location search offers Puducherry, Karaikal and Auroville.
Things that need real services are still local stand-ins: no SMS is sent, messages stay on the
phone, and profile links point at `askit.in/u/...` placeholders.

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
./gradlew assembleDemo               # testable demo APK: app/build/outputs/apk/demo/app-demo.apk
./gradlew assembleRelease bundleRelease

# Tests
./gradlew testDebugUnitTest testReleaseUnitTest      # JVM unit tests (incl. en/ta string parity)

# Static analysis (all fail on new findings; warningsAsErrors is on for lint)
./gradlew ktlintCheck        # ./gradlew ktlintFormat to auto-fix
./gradlew detekt
./gradlew lintDebug lintRelease
python3 scripts/check_hardcoded_strings.py           # no hardcoded user-visible strings
```

The unit tests are a deliberately small plain-JVM suite (no Robolectric, emulator or screenshot
tests): they cover only behaviour that would ship a real bug or a privacy/security problem if it
broke. See CONTRIBUTING.md.

## CI

- `.github/workflows/ci.yml` (push and pull request to `main`): hardcoded-string check, ktlint,
  detekt, Android lint, debug and release unit tests, debug, release and demo assembly.
  Reports are uploaded when a step fails.

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
