# Current-account contract — proposed

The frontend implements this response in `AuthApi.currentAccount`. Agreement and live
backend testing are still required. This is a read endpoint, not signup/profile editing.

## Request and response

`GET /v1/me`, relative to `ASKIT_BACKEND_BASE_URL`, with `Authorization: Bearer <accessToken>`.
No request body or user ID parameter. The server derives the account from the token.
Use HTTPS; redirects are not followed. JSON responses are limited to 64 KiB.

Successful response (`200`):

```json
{
  "userId": "opaque-user-id",
  "phoneNumber": "+919898989898",
  "profile": {
    "displayName": "Kavin",
    "username": "kavin",
    "city": "Madurai",
    "joinedYear": "2026"
  }
}
```

All three top-level fields are required. `userId` must be nonblank and match the
authenticated session. `phoneNumber` is E.164 with ASCII digits, no spaces. For an
account that has not completed signup, return `"profile": null` explicitly; omitting
the field is an invalid response. Profile presence overrides the cached completion flag.

## Profile fields

Required strings: `displayName`, `username`, `city`, `joinedYear`. Name/city must be
nonblank; username may be empty until assigned; joined year is four ASCII digits.
Optional fields must have the following types when present. Only fields marked nullable
accept JSON null. Unknown fields are ignored for compatible response evolution.

| Field | Type / omitted default |
| --- | --- |
| `bio`, `about`, `pincode` | String, empty |
| `avatarUrl` | Nullable HTTPS URL string, null |
| `lookingFor`, `skills`, `licenses` | Array of strings, empty |
| `languages` | Array of `{ "name": string, "proficiency": string }`, empty |
| `profileStrengthPercent` | Integer 0–100, 0 |
| `listing` | Nullable service listing below, null |
| `availability` | Availability object below, default values |
| `gallery`, `reviews`, `savedProfessionals` | Arrays of objects below, empty |
| `followingCount`, `followerCount`, `blockedAccountsCount` | Nonnegative integers, 0 |
| `activeRole` | `Seeker`, `Provider`, or `Both`; default `Seeker` |
| `pushNotificationsEnabled`, `jobAlertsEnabled` | Boolean, true |
| `whoCanMessage` | `Everyone`, `Contacts only`, or `Verified users only`; default `Everyone` |
| `isVerified` | Server-managed boolean, false |

Role/privacy values are stable wire values even when the UI displays Tamil. Free-text
profile/listing content is displayed as supplied. Do not populate missing data with demo
people, counts, skills, or reviews. `isVerified` is not inferred from phone verification,
provider role, or a published service, and must not be accepted in profile edit requests.

### Service listing

Required strings: `title`, `category`, `description`, `quoteLabel`, `coverage`,
`coverageHint`, `hours`, `hoursHint`, `response`, `responseHint`, `experience`.
`tags` is a required array of strings. `categoryId` is an optional nullable string,
default null. `live` is an optional boolean, default true. Display labels use strings
because the current UI supports values such as “Quote on visit”; this endpoint does
not define monetary calculations or booking policy. A non-null listing marks the
profile as having a listed service.

### Availability

All fields optional: `available` boolean (true), `fromMillis` and `toMillis` nullable
integer UTC epoch milliseconds (null), `message` string (empty), `muteNotifications`
boolean (false). Missing availability uses those defaults.

### Gallery item

Required `id` and `uri` strings. `uri` is a remotely readable HTTPS media URL, not an
Android `content://` or `file://` address. Optional `isCarousel` boolean defaults false.

### Review

Required strings `id`, `name`, `meta`, `body`; required numeric `rating` between 0 and
5 inclusive; required integer `createdAtMillis` in UTC epoch milliseconds. Optional
nullable `jobId` and `avatarUrl` strings default null. Avatar URLs use HTTPS.

### Saved professional

Required strings `id`, `name`, `trade`, `rating`. Rating is a display label in this
existing model. Optional nullable HTTPS `avatarUrl` defaults null.

IDs must be nonblank and unique within each gallery/review/saved-professional array.
All media URLs are at most 2,048 characters and must not contain URL credentials.
They must be readable by the image loader; it does not attach the account Bearer token
to media requests. The server remains responsible for media access control.

## Lifecycle and errors

- Cold launch: decrypt stored credentials, refresh, persist the rotated token, then load
  this endpoint. No stored credentials means entry with no authentication request.
- Returning OTP verification: persist credentials, then load this endpoint. Incomplete
  verification opens signup directly; startup rechecks completion using this endpoint.
- A `401`, regardless of response body, triggers one refresh-and-retry. A second `401`
  shows expired-session recovery; it never loops. Other non-200 responses, malformed
  data, or identity mismatch keep account screens blocked with a localized retry error.
- A storage retry saves pending rotated credentials before making another account
  request. It does not reuse a consumed OTP or unnecessarily rotate an old refresh token.
- Sign-in recovery cancels pending work and deletes stored credentials using the logout
  flow. Late network responses cannot restore the previous account.
- Fresh authentication resets saved navigation and creation dialogs. An ordinary
  activity recreation retains the live view model/navigation. If the process dies during
  OTP entry without saved credentials, the app restarts sign-in instead of restoring a
  challenge it no longer has.

The selected app language remains a device preference when replacing the profile.
Local listing drafts are not server profile fields. Current tests use intercepted HTTP,
including storage failures, unauthorized retries, identity mismatch, and cancellation.
This does not implement server-backed signup/editing, uploads, or shared refresh for
other application APIs; see the [remaining frontend work](frontend-handoff.md).
