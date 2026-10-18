# Changelog

All notable changes to the AskIT Android client. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); commits follow
[Conventional Commits](https://www.conventionalcommits.org/).

## [Unreleased] – Code-review cleanup (phases 1–5)

A five-phase cleanup after a full code review, done on stacked local branches
`cleanup/phase-1-hygiene` → `cleanup/phase-5-polish` (integration branch `cleanup/all`).

### Phase 5 – Polish and docs
- **Accessibility:** 48dp touch targets on post actions, comment actions, the profile camera badge,
  story add badge, dashboard dismiss and create-post remove buttons; TalkBack custom actions
  (next, previous, pause) and a "Story X of N" description in the story viewer, which also stays
  paused while touch exploration is on; story avatars announce seen/unseen once; labels for draw
  colours and the shared-video badge; dead unlabelled overflow button removed.
- **Contrast:** WCAG AA for secondary text (no more alpha on `onSurfaceVariant`), the "live" pill
  (was 2.4:1) and story scrims; destructive settings buttons use theme error roles so dark mode
  stays readable. New `contrastRatio()` helper and `ColorContrastTest`.
- **Launcher icon:** adaptive icon with round and monochrome (themed icon) variants.
- **i18n:** listing quote, radius and delivery-mode labels, task labels, account roles and the
  creator-dashboard role fallback are now typed in the model and rendered from string resources
  (English and Tamil). Old saved listings still decode.
- **Refactors:** `ProfileSettingsScreen`, `CreatePostMediaCard`, `ListServiceForm` and
  `LocationSelectionScreen` split into smaller files without behaviour changes.
- **Docs:** README rewritten; new CONTRIBUTING.md, docs/architecture.md and this changelog.
- **Build/test:** removed unused `roborazzi-junit-rule`; de-flaked an auth revoke test.

### Phase 4 – Tests and tooling
- ktlint (android_studio style) and detekt with Compose rules, with baselines, run in CI before
  Android lint.
- `AuthSessionStore` behind an interface; ViewModel flow tests with coroutines-test and Turbine.
- Roborazzi screenshot tests (design-system components and key screens, light/dark, en/ta) with a
  CI record/verify step.
- Compose UI tests for Explore search, Post-a-task review focus, sign-out clearing account data
  and story auto-advance; a non-blocking emulator workflow.

### Phase 3 – Architecture
- Split `ExploreScreen` (2.6k lines) and `PostTaskScreen` into focused files with tests.
- Hand-rolled `AppContainer` + activity-retained `SessionGraph`; view models built from it.
- `AccountScope<T>`: jobs and inbox are scoped to the signed-in account and cleared on sign-out.
- Language and messaging-privacy values named in one place; remaining hardcoded UI strings moved
  to resources; CI fails on hardcoded strings or missing Tamil translations.
- Story draft saves debounced without losing drafts to process death.

### Phase 2 – Correctness
- Demo fixtures compile only into debug and preview (`VariantContent` seam); release has none and
  a release unit test guards it.
- Dashboards, profile defaults and comment/share sheets no longer show fabricated or sample data
  in release.
- Fixes across auth, profile, tasks, jobs, inbox and stories found in the review.

### Phase 1 – Repository hygiene
- History rewritten to remove committed evidence files, stale screenshots and local paths.
- Build, Gradle and CI hygiene; dependency and resource clean-ups.
