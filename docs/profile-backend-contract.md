# Profile API handoff

Scope: Android contracts, no server implementation. Local mutations are enabled only in the client preview; production must return an explicit unavailable state until these endpoints exist. Stable authenticated account IDs, never display names, authorize every write.

## Canonical account and service

Form A: fullName, username, phoneNumber (E.164), email, pincode (Indian six digits, first digit 1–9), preferredLanguage (English/Tamil) required; city and avatar media optional. Optional seeker title/description belong to profile editing, outside signup. GET /v1/me must return these canonical fields, userId, and member-since data. Email verification is optional and independent of required email entry.

Profile PATCH must validate on the server, normalize usernames without changing their stored display prefix, enforce username uniqueness, and atomically return the updated account. Changing phone requires a dedicated authenticated phone-change challenge/OTP confirmation; do not reuse login OTP or label an edited phone verified. Android currently displays verification unavailable and retains the signed-in account.

Form B is the service listing, not a second copy of profile data. Required: title, description, category, published canonical phone/email, whole-year experience 0–60, availability, service delivery/location. Range, certificate and equipment/materials optional. Listing save and contact changes must be atomic; GET account and listing must agree. The backend must persist the listing and authorize owner-only create/edit. Android uses listing.title and listing.description for provider identity; About and My services will read that same record.

Media must be uploaded to account-owned durable storage before publishing; local content URIs are preview data, never public URLs. Return errors without claiming persistence. Existing authentication does not implement these profile/listing mutation services.

Signup creation is also unavailable in production until the server accepts and persists Form A; Android must not navigate to Home after an in-memory substitute. Preview supports the complete local flow for review.

## Banner and location disclosure

Account fields: banner media URL nullable, discloseApproximateLocation boolean default false. Banner upload/remove is owner-only; validate image accessibility, MIME/size and durable media ownership before saving. Cancellation preserves the previous image. Removing a banner sets null and restores the neutral cover.

Public DTO may return bannerUrl and a consent indicator; it must never include owner postcode or exact coordinates. Compute postcodeDistanceKm on the server using postcode centroids only when the owner consents and the authenticated viewer has a valid postcode. Omit distance otherwise and on calculation failure. Android independently checks consent, viewer postcode and finite nonnegative distance, then rounds to whole kilometres. Revoking consent must invalidate cached public distance responses.

## Named highlights

Collections have immutable ID, ownerAccountId, name, ordered story IDs and optional coverStoryId belonging to that collection. Owner-only create/update/delete; reject another account's stories and invalid cover membership. Read access to each retained story must apply its original audience policy, account blocks and media deletion status at playback time. Public covers must come from a story the viewer can access. Without audience authorization Android permits public stories and owner playback only; Close Circle/Message stories stay hidden from other viewers.

Retain explicitly selected story media in durable account-owned storage without modifying ordinary stories' createdAt/expiresAt. Ordinary playback and interactions still expire after 24 hours. Highlight playback uses a separate context and cannot revive normal-story likes/shares. Deleting media or revoking audience access must update/invalidate every collection reference. Return a recoverable unavailable state instead of stale media.

Preview persistence is account-scoped app-private metadata/media; production requires real upload/storage endpoints and authorization. No server is implemented by this change.

## Directional reviews, jobs and relationships

Every received review needs immutable ID, reviewerAccountId, recipientAccountId, jobId, direction (SEEKER_TO_PROVIDER or PROVIDER_TO_SEEKER), rating and text. Authorize against a completed job's stable providerAccountId/seekerAccountId; reject self-reviews, duplicates and mismatched recipients. Unknown legacy identities/directions cannot contribute to either rating. Never turn a review written by the viewer into a review they received. Rating summary and paginated category lists must agree.

Completed summaries are role-specific. Return provider/seeker participant IDs for jobs; display names and local-party UI previews cannot establish identity. Public DTOs may provide completedAsProvider/completedAsSeeker; absent summaries are unavailable rather than invented.

Follower/following lists and counts must come from the same account-ID relationship edges, with pagination, blocked-account filtering and authorization on follow/unfollow. No self-follow. Android reuses the story social store in preview; its relationship graph is durable and account-scoped. Production list/mutation services are unavailable until implemented. Empty lists and unavailable lists are distinct UI states.

## Actions, public URLs and moderation

Owner overflow opens Settings directly. The visitor menu contains About account, Copy profile URL, Share profile, Block and Report only. Visitor mutations require authenticated viewer ID distinct from the target; self public preview disables them. Public preview may inspect published contact details but cannot launch contact actions or perform visitor mutations.

GET /v1/app-config may return publicProfileBaseUrl (HTTPS, no credentials/query/fragment). The configured route accepts an encoded stable account-ID path segment. Missing/invalid configuration yields no public link; never derive a route from the generic website fallback. Published contact actions use Form B's canonical phone/email and open dialer/email composition only.

Profile block/report reuse account-scoped inbox moderation in preview. Production requires authorized account-level moderation endpoints, persistence, report processing and block enforcement across messaging, follows, stories/highlights and public profile access. The current profile UI reports unavailable production services rather than local success. Deferred actions: Hide your story, QR and Restrict.

## Final sections and owner payload

Providers expose fixed IDs `about`, `showcase`, `services`, `tasks`; seekers expose `posts`, `tasks`, even with no content. Both service presentations use the same canonical listing. Showcase/My post use the post repository and task sections filter by `posterId == viewedAccountId`; missing IDs never match by name. Owners may create tasks; visitors cannot create on another account's behalf. Production task submission is unavailable until its server mutation exists.

The private owner account payload includes `listingDraft` (editable Form B), public `listing`, and `highlights`. Listing title/description/contact must agree with the draft and canonical account contacts; Android rejects inconsistent owner responses. Public listings include delivery modes, optional certificate/materials, public service areas and availability. Private draft coordinates never enter public adapters. Highlight collections must match the account ID and satisfy membership/cover invariants. Current Android account decoding enforces these ownership invariants; the server must additionally authorize every retained story/media reference and serve only safe durable media URLs.

Preview retained media copies both content-provider selections and camera cache files into account-scoped app-private files before committing metadata. Signup initializes a separate preview-signup account; it cannot overwrite the previous provider/seeker. Service phone entry is normalized to E.164 before canonical account/listing updates. These local stores demonstrate contracts; they do not implement production persistence or remote services.

Both existing dashboards currently use sample analytics/requests. Profile dashboard actions open those existing flows in preview; production reports unavailable until real analytics/provider operating data is connected. No demo totals are presented as real profile analytics.

## Messaging privacy enforcement

Android preview now evaluates the recipient's saved `whoCanMessage` at conversation creation and every text, photo or story send. `Contacts only` means the recipient's explicit messaging contact list, never followers, an existing conversation, or the sender's contacts. `Verified users only` checks the sender's explicit `isVerified`; supplying a phone/email does not grant verification. Unknown policy/account data, self-messaging and signed-out/stale-account actions are rejected. Blocks in either account's active preview inbox override permission. Preferences use the existing durable account snapshots; preview inbox contacts/blocks remain account-scoped in-memory fixtures, not a durable production contact/block service.

Until messaging services exist, production privacy-setting updates, conversation creation and sending report unavailable and do not append local messages. The server must independently authorize both conversation creation and every send, using the authenticated sender, recipient policy, recipient-owned contact edges, authoritative verification and bilateral blocks in the same mutation. Existing conversations do not bypass a changed policy. Return a structured allowed/restricted/unavailable decision; unavailable authorization must never imply permission. Reading existing history is separate from permission to send. Blocking/reporting does not require permission to message the target. Android keeps an unsent draft when submission is rejected.
