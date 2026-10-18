# Contributing to AskIT Android

## Branches

- `main` is always releasable: CI must be green.
- Work on a short-lived branch named for its type and subject, for example
  `feat/story-replies`, `fix/inbox-unread-count`, `refactor/explore-split`, `docs/readme`.
- Rebase on `main` before opening a pull request; keep pull requests small and focused.

## Commits: Conventional Commits

```
<type>(<optional scope>): <imperative summary, lower case, no full stop>

<body: what changed and why, wrapped at ~72 columns>
```

Types: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `build`, `ci`, `style`, `chore`.
Scopes are feature or module names (`story`, `explore`, `posttask`, `profile`, `designsystem`,
`a11y`, `i18n`, `theme`, ...). One logical change per commit; a refactor and a behaviour change go
in separate commits.

## Strings and translations

- **No hardcoded user-visible text.** Every string a user can see or hear (including
  `contentDescription`, error messages and labels built in view models) comes from
  `res/values/strings.xml`. `scripts/check_hardcoded_strings.py` fails CI otherwise.
- **Models carry typed data, not display text.** Store enums, amounts and ids (for example
  `ListServicePricing`, `AccountRole`, `TaskPublicDetails`) and format them with string resources
  at display time (`Task.presentation`, `ServiceListing.labels`). Persisted or wire values stay
  stable keys, separate from labels.
- **English and Tamil stay in parity.** Every new string in `values/strings.xml` needs a
  `values-ta/strings.xml` entry with the same name and the same format arguments
  (`StringResourceParityTest` fails otherwise). Mark brand names and endonyms
  `translatable="false"`. If you are not sure of a Tamil wording, add your best translation and
  flag it in the pull request for a native-speaker review.

## Accessibility

- Touch targets are at least 48dp (`IconButton`, or `Modifier.minimumInteractiveComponentSize()`
  around a smaller visual).
- Icon-only controls have a `contentDescription`; decorative images use `null`.
- Expose state with semantics (`selected`, `stateDescription`, `Role`), and give gesture-only
  interactions a semantics action.
- Text uses theme colour roles; don't lower their alpha. `ColorContrastTest` checks the theme
  against WCAG AA, and `contrastRatio()` is available for new pairs.

## Tests are required

- New logic gets JVM unit tests (`app/src/test`, JUnit 4, coroutines-test, Turbine).
- Code that needs demo fixtures is tested in `app/src/testDebug`; `app/src/test` runs for release
  too and may only use `src/main`.
- UI flows get Compose UI tests in `app/src/androidTest` where practical.
- Visual changes to shared components: update Roborazzi baselines (`./gradlew recordRoborazziDebug`
  on an x86_64 or macOS machine) and commit the PNGs with the change.

Run before pushing:

```bash
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest testReleaseUnitTest assembleRelease
python3 scripts/check_hardcoded_strings.py
```

## Lint baselines

`config/ktlint/baseline.xml` and `config/detekt/baseline.xml` record existing debt only.

- Don't add to them. Fix new findings instead (`./gradlew ktlintFormat` fixes most ktlint ones).
- When you touch code that has a baseline entry, fix it and delete the entry.
- When a refactor only *moves* baselined code (detekt keys include the file and signature),
  update the matching entry in place rather than adding a new one, and say so in the commit body.
- Regenerating a baseline (`./gradlew detektBaseline`, or ktlint with `--baseline`) is a last
  resort for tool upgrades; review the diff so it doesn't grow.

Android lint runs with `warningsAsErrors` and `abortOnError`; don't disable checks to get a build
through.
