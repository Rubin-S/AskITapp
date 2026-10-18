# Architecture

AskIT is a single-activity Jetpack Compose app in two Gradle modules, `:app` and
`:designsystem`. There is no backend yet: repositories and stores are local and in-memory, shaped
so a network data source can replace them later.

## Object graph

```
AskITApplication
└── AppContainer                      process-wide, stateless
    ├── AppConfig                     backend URL, clientPreview flag (from BuildConfig)
    ├── VariantContent (AppVariant)   demo fixtures in debug/preview, empty in release
    ├── ProfileSnapshotStorage?       preview builds only
    └── viewModelFactory ──────────┐
                                   ▼
MainActivity
└── SessionGraph (activity-retained ViewModel; dropped on sign-out)
    ├── InMemoryTaskRepository        posted tasks
    ├── LocalHomeRepository           stories, feed, services, people (+ StorySocialStore)
    ├── SessionProfileStore           the signed-in account's profile; auth, profile, jobs and
    │                                 dashboards all read this one store
    └── JobsStore                     uses AccountScope<T> per account
```

- **`AppContainer`** (`di/AppContainer.kt`) is created once by `AskITApplication`. It holds only
  things that cannot leak one user's data to another. Its primary constructor takes no `Context`,
  so tests build it on the JVM.
- **`SessionGraph`** (`di/SessionGraph.kt`) holds every stateful store shared by several view
  models. It is a `ViewModel` owned by the activity: it survives configuration changes and is
  discarded with the activity, which is what sign-out does.
- **`viewModelFactory`** builds each screen's view model from the container and the session graph.
  View models never construct their own repositories.

## AccountScope

`session/AccountScope.kt` holds one value per account and exposes the active one as `current`.
Switching account never hands one account's data to another: in production the outgoing
account's value is dropped (sign-out or user switch clears jobs and inbox); the client preview
passes `retainInactive = true` so its local personas keep their data while you switch. The
signed-out value is never retained.

## VariantContent

`variant/VariantContent.kt` is the seam between production code and sample data: seeded tasks,
home stories and feed, explore people, service details, inbox seed, incoming leads, dashboard
samples, preview personas and storage. `AppVariant` implements it twice:

- `app/src/demo/java/.../variant/AppVariant.kt`, compiled into debug and preview, backed by
  fixtures in `com.askit.app.demo` and `com.askit.app.preview`;
- `app/src/release/java/.../variant/AppVariant.kt`, returning empty data.

So `src/main` cannot reference a fixture without breaking `assembleRelease`, and
`ReleaseVariantTest` double-checks the release classpath. See [build-variants.md](build-variants.md).

## Data flow

```
user action ──► Screen (stateless composable)
                  │ callback
                  ▼
               ViewModel ──► repository / store (SessionGraph) ──► StateFlow
                  ▲                                                  │
                  └────────────── collectAsStateWithLifecycle ◄──────┘
```

- Screens are split into a `Route` (gets the view model, collects state, wires navigation) and a
  stateless `Screen` that takes plain state and callbacks, which is what UI and screenshot tests
  drive.
- State is immutable data classes exposed as `StateFlow`; one-off events (navigation, toasts) go
  through callbacks or event flows.
- Models keep typed data (enums, amounts, ids). Display text is produced at render time from
  string resources (`Task.presentation`, `ServiceListing.labels`), so the UI follows the app
  language (English or Tamil) without re-saving data.
- Persisted and wire values (`AppLanguage`, `MessagingAudience`, `AccountRole` stored values) are
  stable keys kept separate from their labels.

## Navigation

Navigation 3: `AskITNav.kt` owns the back stack and `AskITNavEntries.kt` maps each destination key
to its route composable. Ownership checks (for example editing a task) happen in the entry before
the screen is shown.

## Design system

`:designsystem` provides `AskITTheme` (light and dark Material 3 colour schemes), contrast helpers
(`contrastRatio`, `StoryScrimColor`, WCAG constants, checked by `ColorContrastTest`) and shared
components. Components take state and callbacks, use theme colour roles, and are covered by
Roborazzi screenshot tests in light and dark.
