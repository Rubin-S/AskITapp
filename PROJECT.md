# Project: AskIT Unified User Profile Architecture

## Architecture
AskIT is an Android community commerce and local services application built with Jetpack Compose, Material 3, and Kotlin. It consists of two Gradle modules:
- `:designsystem`: Design tokens, Material 3 components, theme (`AskITTheme`), icons, and reusable profile primitives (`com.askit.designsystem.profile`).
- `:app`: Application presentation, domain models, navigation (`com.askit.app.navigation`), session store (`SessionProfile`), owner profile screen (`ProfileRoute.kt`), visitor profile screen (`UserProfileScreen.kt`), and feature panes.

The unified user profile architecture bridges owner and visitor profiles into a single, cohesive presentation pipeline:
```
+-------------------------------------------------------------+
| Data Source: SessionProfile (Owner) / UserProfileData (Visitor)
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
| Domain / UI State Mapper: ProfileUiState
| - Form A (Member): 3 metrics, 3 tabs, member actions
| - Form B (Provider): 4 metrics, 4 tabs, provider actions
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
| UniversalProfileScaffold (Shared Composable in :app / :designsystem)
| 1. Cover (120dp)
| 2. Avatar (96dp, 4dp border, camera affordance in owner mode)
| 3. Identity Block (Name, Verified Badge, Trade Headline, Locality)
| 4. Metrics Bar (Flexible ProfileMetricsBar: 3 or 4 metrics)
| 5. Primary Action Row (Contextual: Owner vs Visitor)
| 6. Optional Form B Motivational Banner (Form A Owner only)
| 7. ProfileSectionTabs (Dynamic tabs based on Form A / Form B)
| 8. Content Section (Pane matching active tab)
| 9. Floating Exit Preview Banner (When isPublicPreview == true)
+-------------------------------------------------------------+
            │                                     │
            ▼                                     ▼
+-----------------------+             +-----------------------+
| Owner: ProfileRoute   |             | Visitor: UserProfile  |
| - In-place preview    |             | - Form A & Form B     |
+-----------------------+             +-----------------------+
```

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| F1 | Universal Spatial Ordering | Header: Cover -> Avatar -> Identity -> Metrics Bar -> Primary Action Row -> Tab Bar -> Content Section | M1, M2, M3 | ORIGINAL_REQUEST §R1 |
| F2 | Contextual Action Switching | Owner sees Edit Profile, Share, View as Public; Visitor sees Message, Follow, and contextual Request Service | M1, M3 | ORIGINAL_REQUEST §R1 |
| F3 | Form A Dynamic Metrics & Tabs | 3 metrics (Activity, Followers, Following); 3 tabs (Activity, About, Reviews); Motivational Form B banner for owner | M1, M2, M3 | ORIGINAL_REQUEST §R2 |
| F4 | Form B Dynamic Metrics & Tabs | 4 metrics (★ Rating & count, Completed Jobs, Followers, Following); 4 tabs (Services, Showcase, Reviews, About); Verified Trade title | M1, M2, M3 | ORIGINAL_REQUEST §R2 |
| F5 | Form A Visitor Protection | Visitor viewing Form A member never sees empty provider tabs or "Request Service" CTA | M2, M3 | ORIGINAL_REQUEST §R2 |
| F6 | In-Place "View as Public" Preview | Owner profile toggles public preview mode with floating "Exit Preview" banner; zero page transition or state loss | M1, M2, M3 | ORIGINAL_REQUEST §R3 |
| F7 | Design System Primitives Refactoring | Refactor/create ProfileMetricsBar, ProfileIdentityBlock, ProfileActionRow, ProfilePreviewBanner, CompleteFormBBanner in :designsystem | M1 | ORIGINAL_REQUEST §R4 |
| F8 | Zero Code Duplication | Refactor UserProfileScreen and ProfileRoute to share UniversalProfileScaffold, eliminating duplicate layout logic | M3 | ORIGINAL_REQUEST §Acceptance Criteria |
| F9 | Test-Driven Quality & Visual Parity | Unit, contract, and Robolectric suites pass; Roborazzi visual regression baselines updated | M4, E2E-Track | ORIGINAL_REQUEST §Acceptance Criteria |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Design System Profile Primitives | Create/refactor ProfileMetricsBar, ProfileIdentityBlock, ProfileActionRow, ProfilePreviewBanner, CompleteFormBBanner in `:designsystem:profile`; add comprehensive unit/contract tests | none | DONE |
| M2 | Domain State Models & Dynamic Engine | Create `ProfileUiState`, metric/tab projection engine for Form A vs Form B, expand mock repository for Form A visitors, build `UniversalProfileScaffold` in `:app` | M1 | DONE |
| M3 | Route Integration & In-Place Preview | Refactor `ProfileRoute.kt` and `UserProfileScreen.kt` to consume `UniversalProfileScaffold`, wire in-place "View as Public" preview mode with floating exit banner | M2 | IN_PROGRESS |
| M4 | Final Milestone: E2E Test Suite & Visual Baselines | Run and pass 100% of E2E test suite (Tiers 1-4), update Roborazzi screenshot baselines, perform Tier 5 adversarial hardening | M3, E2E-Track | PLANNED |
| E2E | E2E Testing Suite Track | Independent requirement-driven opaque-box test track (Tiers 1-4), publishes `TEST_READY.md` | none | DONE |

## Interface Contracts

### `:designsystem` ↔ `:app`

#### 1. `ProfileMetricsBar` (`com.askit.designsystem.profile.ProfileMetricsBar`)
```kotlin
data class ProfileMetricItem(
    val id: String,
    val value: String,
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val iconTint: Color? = null,
    val onClick: (() -> Unit)? = null,
)

@Composable
fun ProfileMetricsBar(
    metrics: List<ProfileMetricItem>,
    modifier: Modifier = Modifier,
)
```

#### 2. `ProfileIdentityBlock` (`com.askit.designsystem.profile.ProfileIdentityBlock`)
```kotlin
@Composable
fun ProfileIdentityBlock(
    displayName: String,
    localityLine: String,
    modifier: Modifier = Modifier,
    tradeHeadline: String? = null,
    isVerified: Boolean = false,
    bio: String? = null,
)
```

#### 3. `ProfileActionRow` (`com.askit.designsystem.profile.ProfileActionRow`)
```kotlin
sealed interface ProfileActionConfig {
    data class Owner(
        val onEditProfile: () -> Unit,
        val onShare: () -> Unit,
        val onViewAsPublic: () -> Unit,
    ) : ProfileActionConfig

    data class Visitor(
        val onMessage: () -> Unit,
        val isFollowing: Boolean,
        val onToggleFollow: () -> Unit,
        val onRequestService: (() -> Unit)? = null, // null for Form A
    ) : ProfileActionConfig
}

@Composable
fun ProfileActionRow(
    config: ProfileActionConfig,
    modifier: Modifier = Modifier,
)
```

#### 4. `ProfilePreviewBanner` (`com.askit.designsystem.profile.ProfilePreviewBanner`)
```kotlin
@Composable
fun ProfilePreviewBanner(
    onExitPreview: () -> Unit,
    modifier: Modifier = Modifier,
)
```

#### 5. `CompleteFormBBanner` (`com.askit.designsystem.profile.CompleteFormBBanner`)
```kotlin
@Composable
fun CompleteFormBBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

### `:app` Internal Contracts

#### 1. `ProfileUiState` (`com.askit.app.profile.ProfileUiState`)
```kotlin
data class ProfileUiState(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val localityLine: String,
    val isVerified: Boolean = false,
    val isProvider: Boolean = false,
    val tradeHeadline: String? = null,
    val bio: String = "",
    val metrics: List<ProfileMetricItem>,
    val tabs: List<ProfileTabSpec>,
    val selectedTabIndex: Int = 0,
    val isPublicPreview: Boolean = false,
    val isFollowing: Boolean = false,
    // Content data
    val listing: ServiceListing? = null,
    val gallery: List<ProfileGalleryItem> = emptyList(),
    val reviews: List<ProfileReview> = emptyList(),
    val lookingFor: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val languages: List<Pair<String, String>> = emptyList(),
    val licenses: List<String> = emptyList(),
    val savedProfessionals: List<SavedProfessional> = emptyList(),
    val activeJobs: List<Job> = emptyList(),
    val profileStrengthPercent: Int = 0,
)
```

## Code Layout
- `designsystem/src/main/java/com/askit/designsystem/profile/`:
  - `ProfileAvatar.kt`
  - `ProfileCover.kt`
  - `ProfileIdentityBlock.kt`
  - `ProfileMetricsBar.kt` (New)
  - `ProfileActionRow.kt` (Refactored)
  - `ProfileSectionTabs.kt`
  - `ProfilePreviewBanner.kt` (New)
  - `CompleteFormBBanner.kt` (New)
  - `YourServiceCard.kt`
  - `ReviewRow.kt`
  - `PhotoGrid.kt`
  - `ChipRow.kt`
- `designsystem/src/test/java/com/askit/designsystem/profile/`:
  - Unit and contract tests for design system profile primitives.
- `app/src/main/java/com/askit/app/profile/`:
  - `ProfileUiState.kt` (New state and mapper)
  - `UniversalProfileScaffold.kt` (New universal layout container)
  - `ProfileRoute.kt` (Owner route integrated with UniversalProfileScaffold)
  - Content panes: `ActivityPane.kt`, `AboutPane.kt`, `ReviewsPane.kt`, `GalleryPane.kt`, `ServicesPane.kt`
- `app/src/main/java/com/askit/app/home/details/`:
  - `UserProfileScreen.kt` (Visitor route integrated with UniversalProfileScaffold)
- `app/src/test/java/com/askit/app/profile/`:
  - `ProfileRouteContractTest.kt`
  - `UserProfileScreenContractTest.kt` (New visitor contract test)
  - `ProfileWYSIWYGParityTest.kt` (New parity contract test)
  - `ProfileScreenshotTest.kt` (Roborazzi baselines)
