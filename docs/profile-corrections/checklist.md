# Profile corrections checklist

All seven tasks implement the approved Android scope. Server work is specified in [backend-contract.md](backend-contract.md), not implemented.

| Requirement | Implementation and verification |
|---|---|
| Shared owner/visitor identity for both roles | Same state mapper and scaffold; Form A name, Form B provider title/description, optional seeker identity; immutable username display; identity tests |
| Form A signup/edit agreement | Shared fields and validation; required name/username/phone/email/postcode/language; optional photo/city; verification never inferred; Form A and owner DTO tests |
| Canonical Form B | One editable draft/public listing; canonical contact updates, E.164 phone, numeric/slider whole years 0–60; required availability/location; service tests |
| Banner pencil | Plain icon; empty banner opens picker directly; existing banner offers Edit/Remove; owner only; controlled phone image QA |
| Disclosure consent | Defaults off; requires consent, viewer postcode and valid server distance; public adapter excludes postcode/coordinates; privacy tests and device round-trip |
| Named highlights | Owner create/rename/cover/membership/delete; visitors play; audience/ownership/deletion checks; durable account snapshots and media copies; expiry unchanged; restart/device and regression coverage |
| Ratings and completed jobs | Separate review directions and stable role-specific job participants; unknown identities excluded; both categories in provider rating flow; metrics tests |
| Followers/following | Shared durable ID graph for counts/lists; empty/unavailable states; eligible follow/unfollow; phone positive follow and account switching |
| Exact contextual actions | Provider/seeker × owner/visitor action sets; existing dashboards/chat/contact; direct owner Settings; five visitor menu entries; action tests and four device views |
| Read-only public preview | Self-follow/report/block disabled; contact launch disabled; no fabricated public URLs; policy tests/device evidence |
| Stable sections | Provider About/Showcase/My services/Post your task; seeker My post/Post your task; empty sections visible; shared posts and owner-ID task filter; section tests/device screenshots |
| Return paths | Root navigation clears visitor entries when opening self; navigation regression and device check |
| Cleanup | Superseded Activity/Gallery/About editors, duplicate gallery side effect, dead mutation paths and unused profile/form resources removed after caller searches |
| Languages/accessibility | English/Tamil strings; scrollable full-label tabs; wrapping action/service rows; 1.5× text device captures; original language/font restored |

Evidence is under [evidence](evidence), with action logs in `actions.jsonl`. Unit tests cover ownership, invalid inputs, empty data, account isolation, persistence failures and cache-file retention. Full lint is tracked separately from the passing scoped resource/accessibility checks; remaining unrelated findings are not hidden by project suppressions.

Final result: 40 tests pass; debug and normal minified preview builds pass; scoped resource/accessibility lint passes. Full lint reports 25 app-wide errors in [the saved report](full-lint-results.txt). The final APK is installed on the connected Samsung A35 and [the profile is open](evidence/final_profile_ready.png). USB stay-awake remains enabled; English, font scale and density are restored. Production profile/media/analytics/relationship services require the documented backend work.
