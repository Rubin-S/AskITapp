# Frontend handoff status

The current code and UI are the baseline. Production languages are English and Tamil.
The backend team should implement an agreed HTTP contract in its own stack; it should not
need to edit Kotlin screens or replace local repositories itself.

## What currently connects to HTTP

The [authentication contract](auth-backend-contract.md) defines OTP requests, verification,
refresh, and revocation. `ASKIT_BACKEND_BASE_URL` configures these calls. Phone/OTP screens
handle request failures, retry limits, and pending requests. Successful verification saves
encrypted refresh credentials. Logout deletes credentials and ends the old app task,
with best-effort server revocation. Tests use intercepted HTTP responses, not a live backend.

The [public app configuration endpoint](app-config-contract.md) supplies admin-editable
terms, privacy, help, and support destinations. These refresh on app foregrounding and
use `https://askit.in` as the owner-selected website fallback. No support email is invented.

Normal launches validate stored credentials before showing account screens. Missing
credentials lead to entry; complete accounts load their [server profile](current-account-contract.md)
before Home, while incomplete accounts open signup. Failed restoration keeps account
screens hidden with recovery actions. The `extra_start_at_home` preview shortcut is
debug-only and is ignored by release builds. The launch tests that exercised both build variants
were removed from the repository in October 2026 (commit "consolidate app flows").

## Frontend work still required before handoff

| Area | Remaining work |
| --- | --- |
| Session lifecycle | Shared refresh coordination and expired-session handling for future authenticated data adapters; live-server lifecycle validation. Startup/current-account loading already has rotation, bounded retry, and recovery. |
| Profiles | Server-backed signup/editing; avatar upload. Signup currently saves only an in-memory profile. |
| Application data | Define contracts and connect jobs, tasks, services, messaging, posts, stories, search, and settings. Current data sources still include fixtures and in-memory state. |
| Story media | Export the edited composition: Download still copies original media and needs safe storage/error handling. Editor, share preview, and viewer now use Media3 video playback with the selected trim range, loading/retry states, and lifecycle cleanup; gallery videos supply their duration to the trimmer. Local publication retains the complete draft and authored viewport. Photo loading/error timing, validation before publication, durable draft storage, actionable stickers/audience routing, and physical media/lifecycle validation still need completion. |
| Account isolation | Verify complete account switching with all real data adapters. The separate “Clear app data” action currently resets only the profile/locale, not all application data. |
| Product audit | Finish auditing hard-coded text, demo-only interactions, and success feedback against real operation outcomes. |
| Device validation | Initial Galaxy A35 checks passed for launch/recreation, English/Tamil, real Keystore, and camera opening/switching/release. Camera permission/error recovery had automated regression coverage until the test suite was removed from the repository in October 2026 (commit "consolidate app flows"); the updated camera still needs physical denial/capture/recording checks. Location, uploads, accessibility, and broader device/lifecycle coverage also remain; see the [device record](device-validation.md). |

These are frontend responsibilities, not backend implementation blockers to hand over unresolved.
See also the [task privacy requirements](task-privacy-handoff.md) for restricted address/access data.

## App-owner release inputs

The owner must publish approved legal/support content and configure its destinations through
the backend/admin dashboard; the fallback home page is not an approved legal policy. The owner
also supplies the upload signing key, Play Console access, and listing declarations.
Unsigned release APK/AAB builds are available; signing instructions
are in the [README](../README.md). A real signed release and store submission have not been verified.
Screenshot assets and test baselines were removed at the owner's request; no screenshot-based
regression coverage is claimed. Store listing assets will need a separate owner decision.

## Handoff acceptance

Before calling this URL-only integration, all application HTTP adapters must be connected,
contracts agreed and exercised against a contract-conforming backend, and the frontend and
owner release checks above completed. A passing build alone does not meet that bar.
