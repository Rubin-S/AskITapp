# Original User Request

## Initial Request — 2026-09-03T00:41:04Z

Build a production-ready, unified User Profile architecture for AskIT that supports the dual-identity community model (Form A community members and Form B service providers) with 100% WYSIWYG (What You See Is What You Get) visual parity between own profile and visitor profile.

Working directory: d:/AskITapp
Integrity mode: development

## Requirements

### R1. Universal Layout Scaffold (1:1 WYSIWYG Parity)
Create a single unified layout structure shared by both `ProfileRoute.kt` (owner view) and `UserProfileScreen.kt` (visitor view):
- **Header**: Profile cover banner, avatar with status/camera affordance, display name, verified community badge, trade/headline, and locality (`Area, City · Joined Year`).
- **Unified Spatial Ordering**: Cover -> Avatar -> Identity (Name/Headline/Locality) -> Metrics Bar -> Primary Action Row -> Tab Bar -> Content Section.
- **Contextual Action Switching**:
  - Owner sees: `Edit Profile`, `Share`, and an in-place `View as Public` preview toggle.
  - Visitor sees: `Message`, `Follow/Unfollow`, and contextual `Request Service` (if provider).

### R2. Dual-Identity Dynamic Metrics & State (Form A vs Form B)
The profile dynamically adapts its metrics and tabs based on whether the user has completed Form B (service listing):
- **Community Member (Form A only)**:
  - Metrics: `Activity` (tasks posted/exchanged), `Followers`, `Following`.
  - Tabs: `Activity`, `About`, `Reviews` (reviews as a client).
  - Owner view includes a motivational banner or action: *"Offer services in your neighborhood — Complete Form B"*.
  - Visitor view never shows empty provider tabs or a "Request Service" CTA.
- **Service Provider & Member (Form A + Form B)**:
  - Metrics: `Rating (★ & count)`, `Completed Jobs`, `Followers`, `Following`.
  - Badge: Verified Trade title (e.g. "Certified Electrician").
  - Tabs: `Services`, `Showcase / Gallery`, `Reviews`, `About`.
  - Visitor view prominently shows `Request Service` and verified trust signals.

### R3. In-Place "View as Public" (WYSIWYG Simulation)
Provide an in-place preview mode on the owner's profile (`View as Public`) that renders the exact visitor perspective, allowing the user to audit their public storefront, active services, and privacy settings before publishing changes.

### R4. Production-Ready Jetpack Compose Architecture
Refactor profile presentation components into clean, reusable design-system primitives in `:designsystem` and `:app`:
- Eliminate arbitrary placement (e.g., `Edit profile` placed above user name).
- Reuse standard Material 3 tokens, `AskITTheme`, and existing icons.
- Ensure strict test-driven quality: all unit tests, contract tests, and Robolectric suites pass.

## Acceptance Criteria

### Visual & Structural Parity
- [ ] The header, identity, metrics bar, action row, and tabs occupy identical vertical positions across own profile and visitor profile.
- [ ] "Followers" and "Following" counts are preserved as essential community metrics for all users.
- [ ] For Form A members, no "Request Service" button or empty provider tabs appear on the visitor view.
- [ ] For Form B providers, ratings, completed job counts, and service cards appear seamlessly alongside community metrics.
- [ ] Tapping "View as Public" on the owner profile renders the exact visitor view with a floating "Exit Preview" banner.

### Code Quality & Testing
- [ ] Zero code duplication between owner profile and visitor profile components.
- [ ] All unit and contract tests in `:app` and `:designsystem` pass with 0 failures (`.\gradlew testDebugUnitTest`).
- [ ] Roborazzi visual regression baselines are updated or validated.
