# Authentication handoff — proposed contract

Status: proposed contract; phone/OTP, credential persistence, startup restoration, and
returning-account loading are wired. The backend has not been implemented or tested live.
This describes the API needed by the current phone, six-digit OTP, and signup screens.
The backend may use any language. Agreement on this contract should precede implementation.

## Current implementation and gaps

- Phone requests use `AuthViewModel` and open OTP entry only after a successful challenge
  response. Failed/unconfigured requests stay on the phone screen with localized errors.
- OTP verification now uses the server challenge and exact entered code. Invalid/expired
  codes show localized errors; signup opens only after a valid response with
  `profileComplete=false`. Returning accounts load their [current profile](current-account-contract.md)
  before Home is shown. A failed profile request offers retry without resubmitting the OTP.
- Resend calls the challenge endpoint and keeps the entered code/challenge on failure.
  Successful replacement clears the old input and starts the server-provided wait.
  The activity-retained view model keeps a monotonic deadline across screen recreation;
  OTP entry displays remaining time rather than owning its own resettable timer.
- Signup saves to the in-memory profile store; it does not create a server account.
- Verification saves encrypted refresh credentials before advancing. Logout removes those
  credentials and discards the old activity and its account-specific state.
- Startup restores stored credentials, rotates and saves the refresh token, then loads the
  current account. Profile requests retry at most once after a 401; repeated rejection
  offers sign-in recovery. Repeated startup/retry actions do not create concurrent loads.
- A fresh authentication result resets saved account navigation. Activity recreation retains
  the active navigation and sign-in state; process death without saved credentials returns
  to entry instead of restoring an unusable OTP or a private screen.
- Authenticated application requests and shared refresh coordination across future data
  adapters are not wired. The current refresh/retry protection covers account bootstrap only.

These are frontend integration tasks, not work to leave for a backend engineer unfamiliar
with Kotlin. Adding a base URL alone cannot activate the current implementation.

`auth/AuthApi.kt` implements the request, verification, refresh, and revoke calls below. It uses the
existing OkHttp version with an explicit dependency. Its shared `network/BackendClient.kt`
transport also serves public app configuration, requires HTTPS, disables redirects
and automatic connection retries, cancels calls with their coroutine, bounds responses
to 64 KiB. `AuthApi` rejects incomplete authentication payloads. Typed errors keep raw response
messages out of the UI. Transport tests use intercepted HTTP requests, not a running
backend. Signup writes, media upload, and the remaining application adapters are still required.

Configure the public HTTPS origin (or path prefix) with the Gradle property
`askitBackendBaseUrl` or environment variable `ASKIT_BACKEND_BASE_URL`. The Gradle property
takes precedence. For example: `./gradlew assembleDebug -PaskitBackendBaseUrl=https://api.example.com`.
Do not put credentials in the URL. Empty configuration builds successfully but cannot
request a sign-in code. This setting currently connects authentication requests
and [current-account loading](current-account-contract.md), as well as
[public app configuration](app-config-contract.md); it does not activate the remaining
in-memory application repositories.

## Credential persistence implementation

`auth/AuthSessionStore.kt` provides encrypted storage for verification and startup
refresh. It retains only the refresh credential, user
ID, and profile-completion flag. Access tokens and entered OTP codes are not persisted.
The credential file is in Android's no-backup directory, encrypted with AES-GCM using
an app-specific Android Keystore key. The canonical backend URL is authenticated with
the ciphertext, so switching backends cannot silently reuse those credentials.

File reads are bounded. Writes are synchronized, flushed, and atomically replaced;
save/delete failures propagate rather than claiming success. Invalid ciphertext or an
unavailable key never falls back to plaintext. Callers must handle these errors and
offer sign-in/recovery without treating the account as authenticated.

Verification advances only after the refresh credential is saved. If saving fails,
the verified response remains in memory for a storage retry, without submitting the
consumed OTP again. Starting a fresh phone sign-in clears prior credentials before
requesting a new challenge; resend does not clear the existing challenge or credentials.

Startup refresh saves the rotated token before requesting `/v1/me`. If storage fails,
retry first saves the pending response without rotating the consumed token again. Access
expiry uses elapsed time and is not extended by a storage retry. Unreadable stored
credentials and offline account loads keep private screens hidden, with retry/sign-in
recovery. An expired session offers sign-in recovery without a looping retry button.

Logout hides account screens immediately, cancels and joins any in-flight authentication
operation, and deletes saved credentials before attempting server revocation. Revocation
has a five-second limit; connectivity/server failure does not prevent local logout. If a key
or credential is unreadable, deletion can still proceed, but server revocation may be impossible.
A deletion failure keeps the account screens blocked and offers a localized retry; it does
not report successful logout. After success, `MainActivity` starts a fresh entry task and
finishes the old activity, disposing its navigation, saved drafts, and account view models.
This does not clear all app files or implement the separate settings “Clear app data” action.
Cancelled sign-in operations cannot publish a previously queued network failure into newer
UI state. This case has a regression test; cancellation and exceptional resumption can race
as described in the [Kotlin coroutine contract](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/suspend-cancellable-coroutine.html).

Storage tests run on Robolectric API 26 and 34 with injected JVM AES keys. They verify
round trips, replacement, fresh encryption, tampering, backend binding, missing keys,
size/format checks, and filesystem failures. Physical-device tests also passed on a
Samsung Galaxy A35 / Android 16 using the real Android Keystore provider: encrypted
round-trip/reopen/rotation/deletion, tamper rejection, and backend binding. These tests
use isolated files and do not replace the user's saved credentials. They do not establish
hardware-backed key storage on every supported device. See the
[device validation record](device-validation.md) and
[Android Keystore guidance](https://developer.android.com/privacy-and-security/keystore).

## Proposed HTTP contract

All requests use HTTPS and JSON. Phone numbers use E.164 format, without display spaces.
The server validates phone numbers; the current client length check is not authoritative.
Codes are strings so leading zeroes are preserved. Never log codes or session tokens.

### Request or resend a code

`POST /v1/auth/otp/challenges`

Request:

```json
{"phoneNumber":"+919898989898"}
```

Successful response (`201`):

```json
{"challengeId":"opaque-challenge-id","expiresInSeconds":300,"retryAfterSeconds":30}
```

The durations above are examples, not client constants. The server owns expiry, resend
limits, and attempt limits. Resend calls this endpoint again after the permitted delay;
the new challenge replaces the old one. Request responses must not reveal whether an
account exists. The client advances only after success and retains the phone on failure.

### Verify a code

`POST /v1/auth/otp/verifications`

```json
{"challengeId":"opaque-challenge-id","code":"012345"}
```

Successful response (`200`):

```json
{"accessToken":"opaque-access-token","refreshToken":"opaque-refresh-token","expiresInSeconds":900,"profileComplete":false,"userId":"opaque-user-id"}
```

`profileComplete=false` opens the existing signup form; `true` opens Home after loading
the current profile. The server binds the challenge to the phone and consumes it on
successful verification. The client must not decide identity from the entered phone or
from a successful local six-digit check. Prevent duplicate submissions while a request
is in flight; display failures without discarding the entered phone.

### Refresh and logout

`POST /v1/auth/sessions/refresh` accepts `{"refreshToken":"..."}` and returns new
`accessToken`, `refreshToken`, and `expiresInSeconds` fields. Refresh tokens rotate;
the server rejects revoked tokens. Authenticated requests use `Authorization: Bearer …`.

`POST /v1/auth/sessions/revoke` accepts `{"refreshToken":"..."}` and returns `204`.
Logout clears local credentials and account-specific data even if revocation cannot
reach the server. The frontend must coordinate concurrent refresh requests and allow
at most one authenticated retry, not an unbounded refresh loop.

## Error response

```json
{"code":"OTP_INVALID","retryAfterSeconds":0}
```

Use `400` for invalid input, `401` for invalid/expired credentials or codes, `429` for
rate limits, and `5xx` for server failure. Distinguish `OTP_INVALID`, `OTP_EXPIRED`,
`RATE_LIMITED`, and `SESSION_EXPIRED` with stable codes. For rate limits, provide the
remaining delay in `retryAfterSeconds`. The frontend maps codes to English/Tamil
resources and uses a generic localized error for unknown codes, malformed responses,
or connectivity failures; it does not display raw server errors.

## Required verification before handoff

- Request failure stays on the phone screen; retry does not create duplicate navigation.
- Verification actually submits the entered code, including leading zeroes.
- Invalid/expired codes do not authenticate; resend failure does not claim a code was sent.
- Rotation/backgrounding does not reset the server-derived resend deadline.
- Returning accounts bypass signup; incomplete accounts retain their signup draft on failure.
- Session restoration, refresh rotation, concurrent expiry, logout, and account switching
  cannot leak a previous account's data or produce an infinite retry loop.
- English/Tamil error states, offline behavior, and real-device lifecycle tests pass.

Profile completion and media upload contracts still need to be specified alongside the
remaining application APIs. Local `content://` avatar URIs must be uploaded, not sent
as remotely readable URLs. Production signing, legal metadata, and device validation
remain separate release requirements.

The profile models now carry an explicit `isVerified` flag, defaulting to false. The
profile API must supply this server-managed status for both the signed-in user and
public profiles. Do not accept it in profile-edit requests or derive it from provider
role, a published service, or successful phone OTP verification. The frontend badge
reflects this field; it does not perform or establish professional verification.
