# Physical-device validation

> **Historical record (September 2026).** The device observations below are accurate for the
> builds tested at the time. The unit and instrumented test suites, test counts, log files and
> Gradle commands referenced here were removed from the repository in October 2026, so the
> commands no longer run as written. Re-validate before relying on any result.

## 2026-09-11 checkpoint

Samsung Galaxy A35 (SM-A356E), Android 16, USB-connected. A fresh debug APK was installed
in place with `adb install -r`; app data was not cleared and the older preview APK was
not used. All checks below used the current code/UI. No screenshots were captured.

| Check | Result / scope |
| --- | --- |
| Normal cold launch | Entry opens; no account UI without authentication. |
| Backend absent | A valid-looking test phone stays on the phone screen with the localized configuration error; no OTP screen or fake success. No SMS request is made. |
| Activity recreation | Phone input and error survive recreation; back returns to entry. |
| Languages | Picker contains only English/Tamil. Switching to Tamil updates settings; the device sign-in test passes in both languages. App language returned to English. |
| Android Keystore | Three instrumented tests pass with the real provider: encrypted save/reopen/rotate/delete; altered ciphertext rejected; wrong-backend decryption rejected. Test files are isolated from saved account credentials. |
| Camera | With permissions already granted, rear camera opens, flip connects front/disconnects rear, and closing disconnects the camera and returns to Home. Verified controls and camera-service state, not image quality. No photo/video captured. |

The instrumentation suite has **four distinct tests**, passed once in English and once
in Tamil (eight executions). Logs are local build outputs:
`build/frontend-usb-tests-english.log` and `build/frontend-usb-tests-tamil.log`.
The camera/profile walkthrough used the debug-only Home preview because the backend
does not exist. This preview still contains sample application data.

## Repository verification at this checkpoint

`lintDebug lintRelease testDebugUnitTest assembleDebug assembleRelease bundleRelease`
passed with 764 unit tests, zero failures/errors, and no issues in all four module/variant
lint reports. The final log is `build/frontend-usb-final-verification.log`. Kotlin compilation
and release shrinking/packaging passed; release artifacts are unsigned.
The three release-only `MainActivityLaunchTest` checks also passed via
`:app:testReleaseUnitTest --tests 'com.askit.app.MainActivityLaunchTest' '-Pandroid.onlyEnableUnitTestForTheTestedBuildType=false'`;
the log is `build/frontend-usb-release-launch.log`. These verify the release build ignores
the debug Home-preview extra and preserves normal sign-in navigation.

Two existing compiler deprecation warnings remain for `LocalClipboardManager` in
`home/HomeScreen.kt` and `home/details/PostDetailScreen.kt`; no lint suppression was
added for this work. During development, new lifecycle tests exposed three scheduler-wait
timeouts. They were corrected to drive Compose's test scheduler consistently, without
removing the cancellation/logout assertions; the full suite then passed.

## Run without clearing the user's app data

Use an authorized, unlocked device; select its serial if more than one device is connected.
The launch test is intended for an unconfigured debug build and is skipped if a backend
URL is configured, so it cannot accidentally send a real OTP request.

```powershell
.\gradlew.bat assembleDebug :app:assembleDebugAndroidTest --max-workers=1 --no-parallel
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.askit.app.test/androidx.test.runner.AndroidJUnitRunner
```

Install failures caused by a signing mismatch are not permission to uninstall or clear
the existing app. Resolve the signing/device choice first. The test runner uses AndroidX
Test dependencies only in the test APK, following the
[AndroidX Test setup](https://developer.android.com/jetpack/androidx/releases/test).
The secure-storage tests use temporary test files and the normal Keystore provider;
they neither overwrite saved session files nor delete the app's Keystore key.

## Not established by this checkpoint

- Live authentication, backend profile/data operations, session expiry against a server,
  full account switching, or upload/download access control.
- Camera/microphone/location permission denial and revocation, capture/recording quality,
  photo picker result handling, or location accuracy.
- TalkBack usability, large-font layouts across all screens, long-running/background
  operations, process death with a real account, or low-memory behavior.
- Older Android versions, other manufacturers, hardware-backed key guarantees,
  a signed/minified production install, or Play Console acceptance.

At that checkpoint, source review found that denied camera permission selected a placeholder
preview and binding/capture failures had no user-facing recovery. The changes below address
those code paths; the earlier granted-permission walkthrough does not validate the new code.

Build/test success and this smoke pass do not yet mean backend-only handoff readiness.

## 2026-09-12 camera and Gallery hardening

The current capture screen requests camera access in context, explains denial, and keeps
Gallery and text creation available without camera permission. Permanently denied access
has a settings action; permissions are checked again on resume. Microphone access is only
requested after the user chooses sound for a video. Recording without sound is explicit.
Recovery text is available in English and Tamil.

Capture controls follow camera readiness and busy state. Cancelled gestures do not take
photos, and accessibility actions can take a photo or start/stop recording. Recording
progress comes from CameraX events, and the existing 15-second limit is enforced by the
recorder. A duration-limit finalization keeps its valid output rather than treating it as
a failed recording. Other capture/storage failures report an error and discard partial files.

Leaving the screen invalidates provider/capture callbacks, clears controller actions,
stops recording, and unbinds only this screen's use cases. Backgrounding also invalidates
pending captures so their completion cannot unexpectedly open the editor after returning.
Gallery readability/type checks run off the main thread and cancel with the screen.

The two new regression suites cover 21 cases: `StoryCaptureScreenTest` and
`StoryCameraLifecycleTest`. They use controlled permission results, camera-provider futures,
and image-save callbacks; they do not prove real sensor/encoder output or system permission
dialog behavior. No screenshot images or baselines were recorded.

Repository verification passed with **785 unit tests, zero failures/errors**, and no issues
in the five app/design-system lint reports. Debug, release, and preview APKs plus the
release AAB built successfully, including optimized release/preview packaging. Three
`MainActivityLaunchTest` checks also passed in each of release and preview (six additional
executions). The final log is `build/camera-final-verification.log`. No tests or dependencies
were removed. Run the same verification with:

```powershell
.\gradlew.bat lintDebug lintRelease :app:lintPreview testDebugUnitTest `
  assembleDebug assembleRelease bundleRelease :app:assemblePreview `
  :app:testReleaseUnitTest --tests 'com.askit.app.MainActivityLaunchTest' `
  :app:testPreviewUnitTest --tests 'com.askit.app.MainActivityLaunchTest' `
  '-Pandroid.onlyEnableUnitTestForTheTestedBuildType=false' `
  --continue --configuration-cache --console=plain --max-workers=1 --no-parallel
```

The initial camera-provider test double targeted the Java static entry point instead of
the Kotlin companion call. Correcting that test double made the intended pending-future
cases exercise the implementation; no lifecycle assertions were removed to obtain a pass.

The USB phone was disconnected during this pass. On the next connected-device pass, verify
first denial and repeated denial, recovery from Settings, one-time permission expiry,
photo saving, silent/audio recording, the duration limit, close/background during capture,
camera switching, missing hardware, and low-storage recovery. Check actual media output,
not only navigation or camera-service state.

Implementation references: [Android runtime permissions](https://developer.android.com/training/permissions/requesting),
[CameraX provider lifecycle](https://developer.android.com/reference/androidx/camera/lifecycle/ProcessCameraProvider),
[recording events](https://developer.android.com/media/camera/camerax/video-capture), and
[valid duration-limit output](https://developer.android.com/reference/androidx/camera/video/VideoRecordEvent.Finalize).

The client APK in `deliverables/` remains the frozen 11 September checkpoint; these changes
do not replace the version already shared for client review.

## 2026-09-13 story composition and publication

Published stories now retain the existing `StoryDraft` as their content instead of copying
only a media URL/caption. This preserves media type, text background, layer order and
transforms, trim range, audience metadata, and reshare style. Ownership is explicit rather
than inferred from the name "You". Publication uses the current profile and unique IDs,
keeps multiple own stories, and works before Home's initial load finishes.

Photo/text rendering is shared between editor, share preview, and viewer. The authored
canvas size, density, and font scale travel with the draft; previews fit that frame without
stretching or reflowing its contents. Read-only rendering cannot edit text, drag layers,
or draw. Saved-state reconstruction retains the viewport and reshare snapshot/style.
Starting a new composition clears unrelated transforms/reshare data. Text created on a
white background remains readable, and trim selection respects the existing 15-second cap,
including safe handling of subsecond clips.

Story/seen updates no longer reset Home pagination. Later page responses use current state
instead of overwriting stories changed during the request. Refresh/retry cancel superseded
collection/page requests, and failed requests retain usable content. Sharing pops the story
editor once; a nested viewer-to-create-to-share test protects the return destination.

New regression coverage is in `HomePublicationTest`, `StoryCompositionTest`, and additions
to `StoryViewModelTest`, `StoryGroupTest`, and `CreationNavigationLifecycleTest`. The rendering
tests inspect semantics, bounds, and transformed touch coordinates; they do not record
screenshots. The navigation test explicitly controls the story timer and waits for route
transitions so automatic clock advancement cannot dismiss the story during assertions.

Final verification passed with **808 unit tests, zero failures/errors**, and no issues in
all five lint reports. Debug, release, and preview APKs plus the release AAB built, including
optimized packaging. The three `MainActivityLaunchTest` cases passed in both release and
preview (six additional executions). The log is `build/story-final-verification.log`:
`BUILD SUCCESSFUL in 16m`, 303 tasks. The command is the same full
lint/test/APK/AAB/variant-launch command shown above. This pass adds 23 regression cases;
no tests or dependencies were removed.

The first full run found a Compose `ModifierParameter` ordering error after editing callbacks
became optional for read-only rendering. Moving `modifier` to the first optional position
fixed it without a lint suppression. The earlier log is `build/story-initial-verification.log`;
that run's tests/builds passed, but its three app lint tasks failed. The initial focused
baseline passed; this was an introduced issue, not a pre-existing failure.

This does **not** establish working video playback or edited-media export. The video branch
still uses `AsyncImage`, Download still copies original media, audience choices are metadata
rather than routed recipients, and interactive stickers need their own behavior audit.
Saved-state restoration is not durable draft storage. Device checks remain necessary for
actual photo/video output, keyboard/rotation behavior, rendering on different screens,
storage failures, and background recovery. The USB phone was not connected during this pass.

Implementation references: [Compose measurement and placement](https://developer.android.com/develop/ui/compose/layouts/custom)
and [Android UI-state persistence and its limits](https://developer.android.com/topic/libraries/architecture/saving-states).
The frozen client APK remains unchanged (SHA-256
`fdf66373f722571e1a7107c9e9e424ea825824e57534cd0063b57af0f235b929`).

## 2026-09-14 story video playback

The editor, share preview, and viewer now use the same Media3 player instead of the video
`AsyncImage` placeholder. Playback applies the selected clip, capped at 15 seconds. Editor
and share previews loop; the viewer's progress and completion follow the player rather than
the photo/text timer. Loading and playback failures have visible feedback and a retry action
in English and Tamil. Returning to a previously completed page starts its clip again.

Only an active page creates a player. Holds, editor tools, swiping, and app backgrounding
pause playback; leaving the active page, stopping the app, or disposing the composition
releases it. Opening the share sheet deactivates the editor player. Playback requests are
updated only when their inputs change, so ordinary recomposition cannot override a
player-initiated pause. Screen-on behavior is limited to foreground playback. Saved state
captures the current playhead even when saving precedes the stop callback.

Gallery selection reads video duration off the main thread, releases the metadata retriever,
and rejects unreadable/invalid-duration media before opening the editor. Long videos now
reach the existing trimmer with their actual duration. Viewer gestures use current callbacks
without restarting a held gesture, and the retry button can consume its own touch instead
of advancing the viewer. The photo/text timer pauses outside the foreground and during pager
scrolling, uses the latest completion callback, and completes only once per item.

This pass adds **22 regression cases**: 16 in `StoryVideoTest`, four in
`StoryPlaybackInteractionTest`, and two gallery cases in `StoryCaptureScreenTest`.
Player tests use Media3's `SimpleBasePlayer` as the controlled platform boundary. They verify
clip configuration, buffering/progress, completion, looping, pause/replay, lifecycle release,
error/retry, and state restoration; they do not verify actual decoding, audio output, or GPU
rendering. Two runtime dependencies were added: Media3 ExoPlayer and Compose UI, both 1.11.0.
No existing tests or dependencies were removed.

The initial broad run exposed a race in the existing `AuthCancellationTest`: its immediate
network failure could already be processed before cancellation was attempted. The fixture
now waits for the first request to finish, gates the verification failure until after launch,
and asserts that verification is still pending before cancellation. Its original no-error
and no-session assertions remain; authentication implementation code was not changed.
The failure record is `build/story-video-initial-auth-failure.xml`, and the initial full log
is `build/story-video-verification.log`. A separate new restoration test reproduced a saved
position of zero instead of 3,456 ms before the playhead saver was fixed
(`build/story-video-saved-state-reproduction.log`). The timer test also needed an explicit
frame/idle synchronization after changing paused state before advancing its manual clock.

Final verification passed with **830 unit tests, zero failures/errors/skips**, plus three
`MainActivityLaunchTest` cases in each of release and preview. All five lint reports are clean.
Debug, unsigned release, and preview APKs and the release AAB built successfully. The log is
`build/story-video-final-verification.log`: `BUILD SUCCESSFUL in 11m 34s`, 303 tasks.
The command is the full lint/test/APK/AAB/variant-launch command shown in the camera section
above. English and Tamil each contain 818 app string keys. No screenshots were recorded.

The USB phone was not detected, and no emulator was configured. Physical checks still need
real gallery/camera clips, audio focus and headphone removal, first-frame rendering,
crop/zoom and share-preview fidelity, rapid navigation, rotation/background recovery, and
corrupt/unsupported media. Download still copies the original file: edited-media export,
safe storage/error handling, photo loading/error timing, durable drafts, and publication/
audience/sticker behavior remain frontend work, not backend-team responsibilities.

Implementation references: [Media3 setup and player lifecycle](https://developer.android.com/media/media3/exoplayer/hello-world),
[Compose video surfaces](https://developer.android.com/media/media3/ui/surface),
[player events](https://developer.android.com/media/media3/exoplayer/listening-to-player-events),
[media metadata](https://developer.android.com/reference/android/media/MediaMetadataRetriever), and
[Compose saved-state verification](https://developer.android.com/develop/ui/compose/state-saving).
The frozen 11 September client APK was not replaced, installed, or repackaged during this pass.
