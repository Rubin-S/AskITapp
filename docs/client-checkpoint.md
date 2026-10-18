# AskIT — Client Checkpoint

> **Historical record.** These notes describe the 11 September 2026 checkpoint APK. Test counts
> refer to the suite that existed then; most of it was removed in October 2026.

11 September 2026 · Android app · Current UI checkpoint

This APK lets you explore the current AskIT design, screens, and workflows. It opens
directly into Home so you can review the experience without a backend login.

## Install and use

1. Open `AskIT-checkpoint-2026-09-11.apk` on an Android phone running Android 8 or later.
2. Follow the phone's installation prompts, then open **AskIT**. No account or OTP is needed.
3. Keep an internet connection for sample photos and external links. Most sample-data
   interactions happen locally, but this is not a fully offline app.
4. Use test information only. Changes are local to your session and may reset after
   closing/restarting the app. They are not saved to a shared backend or delivered to others.
5. “Log out” closes this checkpoint. Reopen AskIT to start again with sample data.

This APK is for Android, not iPhone. If an existing AskIT installation cannot be updated,
contact the project team before uninstalling it, so any local data can be preserved.

## What you can review now

- Home feed, stories, local-service/task cards, and their detail screens.
- Explore browsing, search/filter/sort screens using the current sample listings.
- Profile, edit-profile flow, service-card setup, and “View as Public”.
- Task posting, service listing, and post/story creation screens, including their current
  validation and review steps.
- Messages and job-management screens with sample conversations/jobs. Messages entered
  in this checkpoint stay local; they are not sent to real people.
- Creator/provider dashboard screens and the current sample information they display.
- English and Tamil app-language choices through Profile → Settings → Language.

This is an interactive frontend checkpoint, not a live marketplace. Sample people,
reviews, metrics, messages, availability, and job statuses are not live business data.
Some controls demonstrate local UI state only; confirmation screens do not mean a real
booking, publication, report, notification, or message has reached a server.

## Work completed so far

The current navigation, shared UI components, profile layouts, creation/review flows,
and English/Tamil resources are implemented and can be reviewed in this build.
Cleanup and regression testing have improved consistency without changing the agreed UI.

For the future connected app, the frontend also has authentication request/verification,
encrypted sign-in storage, account restoration, logout recovery, and current-profile
loading code. Terms, privacy, help, and support destinations can be supplied by the
backend; `askit.in` is the temporary website fallback. These connected features are
tested with controlled responses, not a live backend, and sign-in is bypassed in this APK.

The preceding quality checkpoint passed 764 unit tests, debug/release lint, and APK/AAB
builds. Initial Galaxy A35 checks covered English/Tamil sign-in error handling, secure
credential storage, and camera opening/switching/closing. Those checks are not evidence
that every device, permission, or live-service flow is finished.

This exact checkpoint APK also passed its build and lint checks, plus nine launch checks
across checkpoint, debug, and release builds. It was installed on the Galaxy A35 and
checked for Home/Explore/Profile navigation, local messaging, job screens, blank task-form
validation, logout, and reopening without login. The full 764-test suite was not rerun
after adding the checkpoint build; these focused checks do not replace full release QA.

## What remains before public launch

1. Backend implementation and agreed API contracts for the app's real data and operations.
2. Remaining frontend API connections: signup/profile saving, media uploads, jobs,
   tasks, services, messaging, posts, stories, search, settings, and synchronization.
3. End-to-end verification against the real backend, including session expiry, offline
   recovery, account switching, access control, and successful/failed operations.
4. Further frontend hardening: camera/permission/error recovery, remaining text and
   interaction audits, accessibility, lifecycle behavior, and wider device testing.
5. Approved privacy/terms/support content, final release signing, Play Console setup,
   store listing materials, and submission checks.

There is still frontend work as well as backend work. This checkpoint should not be
described as production-ready or as needing only a backend URL before launch.

## Suggested five-minute walkthrough

1. Browse Home and open a task or service detail.
2. Open Explore and try the available search/filter controls.
3. Visit Profile, edit a test field, and use “View as Public”.
4. Tap the centre create button and try a task, service, or post form.
5. Visit Messages and the job tabs, then try Tamil in Settings and switch back.

Please send feedback on the current design, wording, navigation, and workflow expectations.
Keep live-data/backend requests separate from visual feedback so the next checkpoint
can address both clearly.
