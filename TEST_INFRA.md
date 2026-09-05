# E2E Test Infra: AskIT Unified User Profile Architecture

## Test Philosophy
- Opaque-box, requirement-driven derived strictly from `ORIGINAL_REQUEST.md`.
- Tests verify user-observable behavior and semantic accessibility trees, independent of internal implementation.
- Methodology: Category-Partition + Boundary Value Analysis (BVA) + Pairwise Combinatorial Testing + Real-World Workload Testing.

## Feature Inventory
| # | Feature | Source (requirement) | Tier 1 | Tier 2 | Tier 3 |
|---|---------|---------------------|:------:|:------:|:------:|
| 1 | F1: Universal Spatial Ordering (Cover -> Avatar -> Identity -> Metrics -> Actions -> Tabs -> Content) | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ |
| 2 | F2: Contextual Action Switching (Owner: Edit/Share/Preview; Visitor: Message/Follow/Request) | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ |
| 3 | F3: Form A Dynamic Metrics & Tabs (Activity, Followers, Following; Activity, About, Reviews) | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 4 | F4: Form B Dynamic Metrics & Tabs (Rating ★, Completed Jobs, Followers, Following; Services, Showcase, Reviews, About) | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 5 | F5: Form A Visitor Protection (No Request Service CTA, no empty provider tabs) | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 6 | F6: In-Place "View as Public" Preview (Floating Exit Banner, exact visitor perspective) | ORIGINAL_REQUEST §R3 | 5 | 5 | ✓ |
| 7 | F7: Zero Code Duplication & WYSIWYG Parity (Identical semantic structure) | ORIGINAL_REQUEST §R4 | 5 | 5 | ✓ |

## Test Architecture
- **Test Framework**: JUnit 4 + Robolectric 4.16 (Native Graphics Mode) + AndroidX Compose UI Test (`ComposeContentTestRule`).
- **Invocation**:
  ```powershell
  # Clean binary cache first to prevent EOFException
  Remove-Item -Path "d:\AskITapp\app\build\test-results\testDebugUnitTest\binary\*" -Force -ErrorAction SilentlyContinue
  .\gradlew :app:testDebugUnitTest --tests "com.askit.app.profile.e2e.*"
  ```
- **Pass/Fail Semantics**: All test suites must complete with 0 failures, 0 errors.
- **Directory Layout**:
  - `app/src/test/java/com/askit/app/profile/e2e/`:
    - `Tier1FeatureCoverageTest.kt` (≥35 test cases covering F1–F7 in isolation)
    - `Tier2BoundaryAndCornerTest.kt` (≥35 boundary & corner case test cases)
    - `Tier3CombinatorialInteractionTest.kt` (≥7 pairwise feature interaction test cases)
    - `Tier4RealWorldScenarioTest.kt` (≥5 end-to-end user journey test cases)

## Real-World Application Scenarios (Tier 4)
| # | Scenario | Features Exercised | Complexity |
|---|----------|--------------------|------------|
| 1 | Community member (Form A) signs up, views own profile, audits with "View as Public", exits preview | F1, F2, F3, F5, F6, F7 | High |
| 2 | Visitor arrives at Form A community member profile, verifies no Request Service button, taps follow | F1, F2, F3, F5, F7 | Medium |
| 3 | Form A member completes Form B service listing, profile dynamically updates to provider layout | F1, F2, F3, F4, F7 | High |
| 4 | Visitor browses Form B provider, inspects rating, completed jobs, reviews tab, and taps Request Service | F1, F2, F4, F7 | High |
| 5 | Form B provider audits storefront via "View as Public" to verify trade badge and service listing appearance before exiting | F1, F2, F4, F6, F7 | High |

## Coverage Thresholds
- Tier 1: ≥35 test cases (5 × 7 features)
- Tier 2: ≥35 test cases (5 × 7 features)
- Tier 3: ≥7 test cases (covering major feature interactions)
- Tier 4: ≥5 realistic application scenarios
- **Total minimum**: ≥82 test cases
