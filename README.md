# AskIT Android

Minimal production-ready Compose foundation for AskIT.

## Modules

| Module | Role |
| --- | --- |
| `:app` | Single-activity shell (`com.askit.app`) |
| `:designsystem` | Monochrome Material 3 theme, bottom bar, create sheet |

## Stack

Kotlin · Jetpack Compose · Material 3 · Navigation 3 · Coil · ViewModel/StateFlow · Roborazzi · Gradle version catalog · Java 17 · minSdk 26

## Verify

```powershell
.\gradlew lintDebug
.\gradlew testDebugUnitTest
.\gradlew verifyRoborazziDebug
.\gradlew assembleDebug
.\gradlew assembleRelease
```

Record screenshot baselines after intentional UI changes:

```powershell
.\gradlew recordRoborazziDebug
```
