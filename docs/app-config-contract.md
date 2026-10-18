# Public app configuration

Proposed backend contract for admin-editable legal and support destinations. This uses the
same `ASKIT_BACKEND_BASE_URL` as authentication; `askit.in` is a fallback website, **not**
an API origin. The backend can use any language.

`GET /v1/app-config` is public, requires no session token, and returns JSON with status `200`:

```json
{
  "privacyPolicyUrl": "https://askit.in",
  "termsUrl": "https://askit.in",
  "helpCenterUrl": "https://askit.in",
  "supportUrl": "https://askit.in",
  "supportEmail": null
}
```

These are temporary fallback values selected by the owner, not claims that the website
contains approved policies. The owner must publish the actual content and update these
fields through the backend/admin dashboard before release. Do not invent a contact email.

Only HTTPS website URLs without embedded credentials are accepted (maximum 2,048 characters).
Missing, null, or invalid website fields fall back individually to `https://askit.in`.
An optional valid support email opens an email composer; otherwise Contact support opens
`supportUrl`. Missing/invalid email values do not create a guessed address.
If no external handler is available, the app shows a localized error rather than crashing.
Terms and privacy links are available on the entry and phone screens before sign-in, as well
as in Settings. The existing English/Tamil disclaimer wording is retained with clickable links.

The frontend requests configuration when the app enters the foreground. Repeated foreground
events during a pending request share that request. Successful responses replace the links,
so admin changes take effect without an APK update. On network failure, server error, or
malformed JSON, the current process retains its last valid configuration. A fresh process
without a successful response uses the website fallback. No configuration is persisted to disk.

Responses are limited to 64 KiB. The endpoint must return the final response directly rather
than redirecting. Unknown JSON fields are ignored. Keep private admin settings, credentials,
and user data out of this public endpoint. Only authorized administrators may modify the
stored configuration; authorization of those writes is the backend team's responsibility.
