# AskIT production readiness audit

Date: 5 October 2026 (Asia/Kolkata).

> **Point-in-time audit.** Findings reflect commit 50774f0 on 5 October 2026. Test counts, the CI
> test step and the lint result quoted below no longer match the repository: most tests were
> removed in October 2026. The NO-GO decision still stands.

**Decision: NO-GO for production launch.** The frontend has substantial UI and authentication work, but essential account and application operations are still local demonstrations. Passing compilation or lint cannot establish a working production service.

## Scope and evidence

This audit covers the current Android working tree, its two modules, release configuration, local automated checks, and documented device validation. The base commit is 50774f04d3f8a302091cc3785ab8720887645942 on feat/home-screen. Extensive existing modifications and untracked source files are included; this is not an audit of that commit alone.

Findings below distinguish confirmed implementation gaps from missing validation. No backend implementation, production environment, Play Console account, upload key, moderation operation, or live API was available for inspection. No device was connected during this audit. Backend security and operational readiness are therefore **unverified**, not assumed to pass or fail.

Priority means:

- **P0:** fundamental launch blocker.
- **P1:** resolve before shipping the affected feature, or remove that feature from the release and re-audit the reduced scope.
- **P2:** improvement or evidence gap that needs an explicit owner and disposition.

There are **18 findings: 2 P0, 14 P1, and 2 P2**. These are readiness priorities, not vulnerability severity scores. Several findings share the same unfinished backend integration.

## Verification record

| Check | Result and limit |
| --- | --- |
| Debug/release lint | Passed in this session; zero issues in the four app/design-system reports. |
| Full debug unit test command | Failed. App test workers aborted with exit 134/SIGABRT after native JniConstants errors about java/nio/ShortBuffer and java/nio/FloatBuffer. The root cause was not established. |
| Recorded debug test results | Design system: 137 passed. App: 362 passed, 2 skipped, no recorded assertion failures. The aborted app suite is incomplete; these counts are not a full-suite pass. |
| Optimized release APK/AAB and release launch tests | assembleRelease and bundleRelease passed; all three release MainActivityLaunchTest cases passed with no failures/errors/skips. This verifies packaging and release-variant unit behavior, not a signed-device launch or live backend. |
| Native alignment | The inspected release APK contains 12 native libraries. All six arm64-v8a/x86_64 libraries have LOAD alignment of at least 16,384 bytes and are uncompressed. APK zip alignment check with -P 16 passed. This is static artifact evidence, not a 16 KB device or Play-generated APK test. |
| Signing/configuration | The inspected APK is unsigned. Generated release configuration has DEBUG=false, CLIENT_PREVIEW=false, and an empty backend URL; its merged manifest contains DEFAULT_API_KEY for Places. |
| Localization | English/Tamil string keys match: app 818/818, design system 88/88. Translation quality and hard-coded text remain separate checks. |
| Physical/runtime validation | None performed in this audit. Existing [device records](device-validation.md) describe a limited Galaxy A35/Android 16 checkpoint and explicitly list outstanding media and lifecycle checks. |
| Visual regression | Screenshot baselines were intentionally removed. No pixel comparison coverage is claimed and no screenshots were recorded. |

Evidence logs are retained under build/production-readiness-audit/. They are local build outputs and are not committed. The full-suite failure is in analysis-checks.log, lint in analysis-lint.log, release verification in release-rerun.log, and native alignment in alignment.json.

## Findings and acceptance criteria

### A01 — P0: Application operations still use fixtures and memory

**Confirmed.** [MainActivity](../app/src/main/java/com/askit/app/MainActivity.kt), lines 145 and 220, constructs sample search results, InMemoryTaskRepository, and FakeHomeRepository. [InboxStore](../app/src/main/java/com/askit/app/inbox/InboxModels.kt), line 31, and [JobsStore](../app/src/main/java/com/askit/app/jobs/JobsStore.kt), line 9, retain local state and seed demo data. Dashboards also construct synthetic metrics and requests. HTTP adapters currently cover authentication, current-account reads, and public app configuration.

**Impact:** a signed APK and API URL do not provide functioning tasks, services, messaging, posts, discovery, or job coordination. Local changes are not available on another device and can disappear with process termination.

**Acceptance:** agree contracts for each shipped feature; connect authenticated adapters; use server-issued identities; exercise create/read/update flows between two real accounts and after process restart. Remove fixtures from production entry points and coordinate shared session refresh for application requests.

**Owners:** Android and backend.

### A02 — P0: Signup and account editing do not persist to the server

**Confirmed.** [Signup navigation](../app/src/main/java/com/askit/app/AskITNavEntries.kt), line 200, opens Home after LocalProfileRepository.completeSignup. [SessionProfileStore](../app/src/main/java/com/askit/app/session/SessionProfile.kt), line 99, only replaces a StateFlow value. [ProfileRepository](../app/src/main/java/com/askit/app/profile/ProfileRepository.kt), line 44, delegates edits locally. Phone changes similarly update local profile text without a new verified server identity.

**Impact:** successful-looking signup does not complete the server account; returning-account restoration cannot recover the locally entered profile. Edits and avatar selections are not durable account changes.

**Acceptance:** implement authenticated signup/edit endpoints, upload avatars, validate phone changes through a server verification flow, and update the canonical account response. Keep the form recoverable on failure; show completion only after acknowledged persistence. Verify signup followed by cold restart and a second-device login.

**Owners:** Android and backend.

### A03 — P1: Task creation loses important fields and forces on-site applications

**Confirmed.** [TaskRepository](../app/src/main/java/com/askit/app/task/TaskRepository.kt), line 30, does not carry the selected work mode or date into Task and does not set posterId. [Apply navigation](../app/src/main/java/com/askit/app/AskITNavEntries.kt), line 562, explicitly passes JobWorkMode.OnSite. [Task detail](../app/src/main/java/com/askit/app/home/details/TaskDetailScreen.kt), line 122, disables Message when author identity is missing.

**Impact:** remote tasks become on-site jobs; the chosen schedule is lost; newly created tasks cannot identify a chat recipient.

**Acceptance:** round-trip work mode, schedule, materials/item requirements, and stable author identity through creation, public projections, detail, and application. Verify a remote task produces a remote job and a dated task retains its exact date. Derive ownership from the authenticated account.

**Owners:** Android and backend.

### A04 — P1: The promised private task-details workflow is absent

**Confirmed gap.** Public Task intentionally excludes exact location and access instructions, which is a useful protection. However, the creation repository does not retain restricted fields or expose an authorized owner/assigned-provider retrieval path. See [task privacy handoff](task-privacy-handoff.md).

**Impact:** users can enter private job instructions that have no usable handoff to the accepted provider. The UI's privacy promise has not been validated against server authorization.

**Acceptance:** separate public and restricted fields on the server. Test owner access, pre-acceptance denial, assigned-provider access after acceptance, unrelated-account denial, reassignment revocation, and cache clearing after logout/account switching. Exclude restricted fields from public responses, notifications, and telemetry.

**Owners:** Android and backend.

### A05 — P1: Job authorization and verification are local demo controls

**Confirmed.** [JobDetail](../app/src/main/java/com/askit/app/jobs/ui/JobDetail.kt), line 203, exposes View as other; [JobsStore](../app/src/main/java/com/askit/app/jobs/JobsStore.kt), lines 28 and 67, uses that display role to permit mutations. Job OTPs are created locally and compared locally, without server expiry or attempt enforcement. ProviderDashboardViewModel maintains a separate job list; its generated IDs are passed to navigation that reads JobsStore.

**Impact:** a single local installation can act as both booking parties. Dashboard jobs can navigate to IDs absent from the main store. These controls do not establish real multi-account authorization.

**Acceptance:** remove role simulation from production; unify job data; authorize every transition on the server using stable party IDs. Define OTP expiry, attempts, and one-time use. Test illegal transitions, replay, duplicate requests, two-party completion, and dashboard-to-detail navigation.

**Owners:** Android and backend.

### A06 — P1: Clear app data reports success without clearing the app

**Confirmed.** [Settings handler](../app/src/main/java/com/askit/app/AskITNavEntries.kt), line 452, resets only profile and locale. [SessionProfileStore.resetAppData](../app/src/main/java/com/askit/app/session/SessionProfile.kt), line 234, restores demo profile defaults. [ProfileViewModel](../app/src/main/java/com/askit/app/profile/ProfileViewModel.kt), line 103, emits the data-cleared message. Credentials, tasks, jobs, inbox, other ViewModels, and capture files are not reset by this action.

**Acceptance:** define this action's intended semantics; implement a coordinated reset and accurate feedback. Verify no demo identity replaces a real account and no prior account state remains accessible. Keep local reset, sign-out, and account deletion as clearly described user actions.

**Owner:** Android.

### A07 — P1: Post and social actions do not correspond to completed operations

**Confirmed.** [Create-post completion](../app/src/main/java/com/askit/app/AskITNavEntries.kt), line 307, appends photo URIs to the profile gallery; the main app supplies no post-publication implementation. [Home sharing](../app/src/main/java/com/askit/app/home/HomeScreen.kt), line 223, displays a sent message without inserting a message or making a delivery request. CommentsBottomSheet defaults to sample comments and composition-local state; Home and PostDetail do not connect its comment callback. Share links use askit.app while legal/support fallback uses askit.in; app-link routing and corresponding web resources have not been established.

**Acceptance:** persist complete posts, polls, comments, reactions, and saves; reconcile acknowledged outcomes with UI state. Deliver internal shares to a real conversation and show pending/failure states. Establish and test the canonical share-link destination. A text-only post must remain visible after navigation and restart.

**Owners:** Android and backend; domain owner for links.

### A08 — P1: Trust and review attribution are incorrect

**Confirmed.** [ProviderDashboardViewModel](../app/src/main/java/com/askit/app/providerdashboard/ProviderDashboardViewModel.kt), line 30, sets isVerified=true regardless of the server-managed profile flag; the dashboard displays this value. [Review callback](../app/src/main/java/com/askit/app/AskITNavEntries.kt), line 530, appends the local user's submitted review to their own profile with the counterpart's name. ReviewsPane then includes it in that profile's rating.

**Impact:** unverified users receive a verified presentation, and outgoing reviews appear as incoming testimonials.

**Acceptance:** use canonical server verification status across all surfaces. Persist reviewer ID, subject ID, and job ID separately; accept reviews only from eligible completed-job participants; display them on the subject's profile. Verify the author and rating aggregation with two accounts.

**Owners:** Android and backend.

### A09 — P1: Story download is not edited-media export

**Confirmed.** [StoryRoute](../app/src/main/java/com/askit/app/story/StoryRoute.kt), lines 83 and 129, copies the source stream rather than rendering the composition. Copying and MediaStore operations run directly from the UI callback, without error handling or failed-row cleanup. The API 26–28 branch writes external media while the manifest declares no legacy write permission; Android documents the legacy storage requirements in its [media-storage guidance](https://developer.android.com/training/data-storage/shared/media).

**Impact:** exported files omit text, drawing, stickers, crop/zoom, and trim; text-only stories have no export. Large copies can block the UI, and storage failures can escape the callback or leave incomplete media.

**Acceptance:** render photo/text compositions and trimmed video, including authored transforms. Perform work off the UI thread; handle cancellation, denied storage, low space, unsupported media, and stream failure; delete incomplete destinations. Verify exported pixels, duration, audio, and saved-file visibility on supported Android versions.

**Owner:** Android.

### A10 — P1: Drafts and media are not durably managed or uploaded

**Confirmed.** [StoryViewModel.markDraftSaved](../app/src/main/java/com/askit/app/story/StoryViewModel.kt), line 310, is a no-op. Draft reconstruction uses SavedStateHandle; captures live in cache/story-captures ([StoryCamera](../app/src/main/java/com/askit/app/story/capture/StoryCamera.kt), line 332). Photo-picker permission failures are swallowed. Avatars, gallery items, chat photos, and publications use local URIs without an upload pipeline.

**Acceptance:** implement account-scoped durable drafts, retained media ownership, upload progress/retry/cancellation, and explicit handling of lost URI access. Verify a saved draft after force-stop and cold launch, and uploaded media from another account/device. Clear obsolete cache files without invalidating active drafts.

**Owners:** Android and backend/media service.

### A11 — P1: Reporting and blocking do not provide production moderation

**Confirmed.** [InboxStore](../app/src/main/java/com/askit/app/inbox/InboxModels.kt), lines 83 and 88, only marks a local conversation blocked/reported. No report reaches a moderation operation. Public post overflow has no supplied reporting action; a detail overflow button is empty. These are insufficient for the intended public posts/stories and direct messaging.

Google Play's [UGC policy](https://support.google.com/googleplay/android-developer/answer/9876937?hl=en) requires effective moderation, appropriate in-app reporting/blocking, and acceptance of terms before UGC creation.

**Acceptance:** implement durable content/user reports, server-enforced blocking, report acknowledgement, and an operated moderation queue with action and appeal/support ownership. Test blocked-user messaging and visibility across devices and after restart; cover posts, stories, comments, and chat.

**Owners:** Android, backend, and product/moderation operations.

### A12 — P1: Account deletion is missing

**Confirmed absence in the inspected client.** Settings exposes logout and Clear app data, but no account-deletion request or external deletion destination exists in the app configuration contract.

For the intended service that creates accounts, Google Play requires an in-app deletion path and an external web resource, with associated data deletion and disclosed retention exceptions. See [account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en). Local-only offline accounts have a different policy scope; that exception does not establish readiness for the planned server accounts.

**Acceptance:** provide the in-app request and external resource; implement identity verification, server deletion, token revocation, local cleanup, status/error feedback, and documented retention. Exercise deletion and subsequent attempts to access the account from another session.

**Owners:** Android, backend, and app owner.

### A13 — P1: Approved legal content and store disclosures are not established

**Confirmed release-input gap.** [AppLinks](../app/src/main/java/com/askit/app/config/AppLinksViewModel.kt), line 19, defaults every website destination to askit.in. [App-config contract](app-config-contract.md) explicitly states this is not approved legal content. Approved policies, support destinations, retention declarations, and submitted Data safety/content-rating answers were not available.

Google Play's [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en) requires an appropriate privacy policy and accurate disclosures covering app and third-party SDK data practices.

**Acceptance:** publish owner-approved privacy/terms/support/deletion resources and configure them. Document phone, precise/coarse location, camera/microphone, uploads, messaging, third-party SDKs, retention, and deletion practices. Verify pre-sign-in access and complete Play declarations against the actual implementation.

**Owner:** App owner, supported by Android/backend.

### A14 — P1: Privacy/audience and notification preferences lack effective behavior

**Confirmed.** whoCanMessage and locationServicesEnabled are profile values without enforcement in messaging or location acquisition. LocationHelper checks Android permission/provider status, not the app setting. Story audience values are retained as metadata; Message has no recipient selection, and all choices use the same local publication path. Notification/job-alert settings update local profile values; no push registration/delivery path or Android notification permission flow was found.

**Acceptance:** enforce messaging and audience access on the server and in caches; make the location setting control the app's acquisition flow. Wire notification permission, token registration, delivery, routing, and persisted opt-outs if notifications ship. Do not present unimplemented destinations/preferences as effective controls.

**Owners:** Android and backend.

### A15 — P1: The release validation gate is incomplete

**Confirmed failed evidence.** The current full debug suite aborted in native test workers; recorded partial passes do not satisfy the existing CI test step. Its cause is unresolved. Earlier September full-suite results in device-validation.md cannot replace current evidence. Screenshot regression coverage is intentionally absent. No device was connected for current real-media, location, accessibility, or lifecycle checks.

**Acceptance:** diagnose the worker crashes and obtain a complete reproducible suite result with accounted-for skips. Keep release sign-in boundary tests. Validate camera denial/capture/recording, actual playback/audio/export, storage/URI failures, coarse-only location, process death/account switching, TalkBack, large text, and English/Tamil on physical devices. Exercise signed, optimized artifacts and a 16 KB environment; static alignment is only one part of [Android's guidance](https://developer.android.com/guide/practices/page-sizes).

**Owners:** Android and QA/release engineering.

### A16 — P1: Production configuration and distribution have no final acceptance gate

**Confirmed.** [Build configuration](../app/build.gradle.kts), lines 15, 48, and 78, permits an empty backend URL, unsigned release builds, and a debug-key-signed preview using the same application ID. These are documented development capabilities, but no production publishing gate checks configuration, fixture exclusion, signing identity, or owner-approved resources. The inspected artifact has an empty backend URL and DEFAULT_API_KEY. Version code remains 1.

**Acceptance:** create a reproducible production build from a reviewed commit with the intended origin, restricted Places key if that feature ships, owner upload signing, and a version code accepted by the existing Play track. Verify production entry rules, signed install/update, Play-generated APKs, mappings, and artifact provenance. Ensure the preview cannot be selected for publication. Build success and unsigned AAB generation alone are insufficient.

**Owners:** Android/release engineering and app owner.

### A17 — P2: Production operations have no evidence in this audit

No live service, crash/ANR monitoring setup, operational dashboards, incident owner, backup restoration results, capacity evidence, or rollout/rollback record was available. The Android project contains no dedicated crash-reporting integration found in this inspection. That does not prove operational monitoring is absent elsewhere.

**Acceptance:** assign ownership and provide monitoring, privacy-safe error diagnostics, support escalation, rollout/rollback, and backend backup/restore/load evidence appropriate to the shipped service. Audit server-side OTP abuse limits, authorization, media access, retention, and dependency exposure when the backend exists. A specific vendor is not required.

**Owners:** Backend/operations and app owner.

### A18 — P2: Maintainability and localization need a final product pass

ExploreScreen is about 2,580 lines, PostTaskScreen 1,531, and ListServiceScreen 1,439. MainActivity manually constructs many feature dependencies, while several stores diverge in identity and ownership. String-key parity is good, but demo-generated labels, dollar-denominated task budgets, English activity text, and hard-coded dashboard content remain. Two deprecated LocalClipboardManager usages remain.

**Acceptance:** prioritize shared identity/data boundaries and extract UI sections where this reduces integration risk. Audit all shipped text and monetary formatting in English/Tamil, verify translation meaning and large-text layout, and replace deprecated APIs as routine maintenance. File length alone is not a release blocker.

**Owners:** Android and product/localization.

## Controls already present

- Auth refresh credentials use AES-GCM with an Android Keystore key, bounded reads, atomic replacement, backend-bound authenticated data, and no plaintext fallback. Credentials are excluded from backup.
- BackendClient requires HTTPS, disables redirects, bounds JSON responses, applies a call timeout, and propagates coroutine cancellation.
- Account bootstrap checks account identity, bounds unauthorized retry, saves rotated credentials, and keeps private UI hidden on restoration failure.
- Logout blocks account UI immediately and clears credentials before best-effort revocation. The separate Clear app data defect does not invalidate that logout design.
- Release disables the debug Home shortcut; preview is explicitly a separate build type. Optimized packaging and lint are configured.
- Camera permission recovery and Media3 player lifecycle have automated coverage. Real device/media output remains a separate acceptance gate.

These controls support the implementation; they do not establish backend or product readiness.

## Order of work and release acceptance

1. **Account and data foundation:** resolve A01–A02; agree canonical identities, API contracts, uploads, refresh coordination, and durable storage.
2. **Correct shipped behavior:** resolve A03–A10 and A14; verify two-account tasks/jobs/chat, trustworthy reviews, real publication, privacy routing, and media output.
3. **Policy and owner inputs:** resolve A11–A13 and A16; establish moderation, deletion, legal/support resources, production configuration, and signed distribution.
4. **Evidence:** resolve A15; complete automated and physical acceptance against the same optimized signed build and contract-conforming service. Assign dispositions for A17–A18.

Launch acceptance requires both P0 findings closed and every P1 closed or removed from the shipped scope with a fresh audit. Missing backend/Play/device evidence must be supplied; do not interpret an untested area as a pass. Completion should be recorded against an exact commit and artifact hash with the responsible owners' evidence.

## Final verification addendum

The initial combined release run was interrupted before a release-test result was available. The bounded rerun completed with exit 0: BUILD SUCCESSFUL in 16s, 111 actionable tasks, with packaging tasks reused where up to date.

Command:

    ./gradlew assembleRelease bundleRelease :app:testReleaseUnitTest --tests 'com.askit.app.MainActivityLaunchTest' '-Pandroid.onlyEnableUnitTestForTheTestedBuildType=false' --console=plain --max-workers=2

Release launch result: 3 tests, 0 failures, 0 errors, 0 skips. These cases run release-variant code under Robolectric; they do not install the optimized APK or exercise a real sign-in service.

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| app/build/outputs/apk/release/app-release-unsigned.apk | 5,930,180 | f2dc29e78e5953a9b13cde5335dab37ae17d34bc7980168bd6b15da0267cd10f |
| app/build/outputs/bundle/release/app-release.aab | 13,685,991 | 279fae7d4250c9db02b2d4b4bfb115fb0c399648e6a75a734a58f210d465f3d8 |

APK zipalign -c -P 16 4 returned 0. APK signature verification returned 1 with a missing signature manifest, consistent with the intentionally unsigned artifact. The inspected AAB has no JAR signature entries. Owner-signed distribution remains unverified.

The successful release checks do not change the NO-GO decision: A01–A16 remain open, and the full debug suite's native worker abort remains unresolved. Only this audit document and local evidence outputs were authored; application source and existing screenshots were not changed.
