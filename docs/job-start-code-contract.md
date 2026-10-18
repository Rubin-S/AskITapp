# Job start-code contract — proposed

Status: **proposed, not implemented by any backend yet.** The Android client currently uses a
local placeholder (`jobs/JobStartCode.kt`, `LocalJobStartCodeAuthority`) that mirrors the rules
below so the UX does not change when the server lands.

## Why

An on-site job starts when the provider enters a short code that only the seeker can see. If the
code is generated, stored or checked on the provider's phone, the provider can start (and later
complete) a job without the seeker. The server must own the code.

## Endpoints (relative to `ASKIT_BACKEND_BASE_URL`, Bearer access token, HTTPS, no redirects)

`POST /v1/jobs/{jobId}/start-code` — **seeker only.** Issues (or re-issues) the code.

```json
{ "code": "4821", "expiresAtMillis": 1760000000000, "attemptsRemaining": 5 }
```

- `403` if the caller is not the job's seeker; `409` if the job is not `Accepted`/`OnTheWay`.
- Re-issuing invalidates the previous code and resets attempts.

`POST /v1/jobs/{jobId}/start` — **provider only.** Body `{"code":"4821"}`.

- `200` with the updated job (`status: "Started"`, `startedAtMillis`).
- `401` `{"code":"START_CODE_INVALID","attemptsRemaining":3}` for a wrong code.
- `410` `{"code":"START_CODE_EXPIRED"}` after expiry (suggested 10 minutes).
- `423` `{"code":"START_CODE_LOCKED"}` after 5 wrong attempts; the seeker must re-issue.
- `403` if the caller is not the job's provider.

## Rules

- Codes are 4 ASCII digits from a CSPRNG, stored hashed, compared in constant time.
- The code is never included in job objects returned to the provider.
- Attempt counting and lockout are per job and server-side; the client only displays them.
- Starting is idempotent: a second valid `start` on a `Started` job returns `200` unchanged.

## Client placeholder (today)

`LocalJobStartCodeAuthority` generates codes with `SecureRandom`, compares with
`MessageDigest.isEqual`, and locks entry after 5 wrong tries per job (`StartCodeCheck.LockedOut`).
`Job.otp` still exists locally so the seeker's "Show code" screen works; remove it from `Job`
when the endpoints above exist.
