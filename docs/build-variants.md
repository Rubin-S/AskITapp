# Build variants and demo content

| Variant | App ID | `CLIENT_PREVIEW` | Demo fixtures | Notes |
|---|---|---|---|---|
| `debug` | `com.askit.app` | false | yes (`src/demo/java`) | Sample feed, people, chats, jobs and dashboards for local development. No preview account switcher. |
| `preview` | `com.askit.app.preview` | true | yes (`src/demo/java`) | Client-preview APK: everything in debug plus preview personas, OTP `123456`, seeded task/post/jobs, local profile snapshots. |
| `demo` | `com.askit.app.demo` | true (+ `DEMO_BUILD`) | yes (`src/demo`, its default source set) | Testable demo APK "AskIT Demo": `initWith(preview)`, R8, non-debuggable, signed with the throwaway `app/demo-keystore/askit-demo.jks` (password `askit-demo`, public on purpose; never use it for anything else). Starts on Welcome with "Explore demo", shows the test OTP, adds the DEMO chip screen menu and "Reset demo data". |
| `release` | `com.askit.app` | false | **no** (`src/release/java`) | Only real/local data. Screens without a backend show empty or "not available yet" states. |

## How it works

- Production code depends on `com.askit.app.variant.VariantContent` (in `src/main`).
- `com.askit.app.variant.AppVariant` is defined twice with the same name:
  - `app/src/demo/java/.../variant/AppVariant.kt` — debug + preview (added with
    `kotlin.directories += "src/demo/java"` in `app/build.gradle.kts`), backed by fixtures in
    `com.askit.app.demo` and `com.askit.app.preview`.
  - `app/src/release/java/.../variant/AppVariant.kt` — release, returns empty data.
- Release therefore cannot compile against a fixture: if `src/main` references anything in
  `src/demo/java`, `assembleRelease` fails.
- `app/src/testRelease/.../ReleaseVariantTest.kt` (run by `testReleaseUnitTest`) also fails if any
  fixture class is on the release classpath or `AppVariant.hasDemoContent` is true.
- Tests that need fixtures live in `app/src/testDebug`; shared tests in `app/src/test` must only
  use `src/main`, because they run for both debug and release.

## What lives where

- `src/demo/java/com/askit/app/demo/`: Home stories/posts/services/people, Explore people, sample
  tasks, inbox contacts and chats, incoming leads and preview jobs, service details, provider and
  creator dashboard numbers.
- `src/demo/java/com/askit/app/preview/`: preview personas (`previewProfile`), seeding,
  `PreviewProfileStorage` (SharedPreferences snapshots), sample public profiles, the preview
  account switcher.
- `src/main`: `LocalHomeRepository`, `InMemoryTaskRepository`, `InboxStore`, `JobsStore` and the
  dashboard view models. They start empty and accept fixtures only through constructor arguments.

## Demo build (`DEMO_BUILD`)

- `BuildConfig.DEMO_BUILD` is true only in the `demo` build type; it implies `CLIENT_PREVIEW`.
- Because the build type is named `demo`, AGP uses `src/demo` as its default source set: the
  fixtures, `src/demo/res` and `src/demo/AndroidManifest.xml` (app label) come in automatically.
  Debug and preview keep adding only `src/demo/java` and `src/demo/res`, so the manifest label is
  demo-only.
- Demo-only UI lives in `src/demo/java/com/askit/app/demo/DemoMenu.kt` / `DemoScreen.kt` and is
  reached through `VariantContent.DemoBadge` / `DemoSettings`; release implements both as no-ops,
  and debug/preview render nothing because they check `DEMO_BUILD`.
- Auth: `AuthViewModel(autoSignInPreviewOwner = !demoBuild)`; "Explore demo" calls `enterDemo()`.
