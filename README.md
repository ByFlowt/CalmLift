# CalmLift 🧘‍♂️🏋️

**A minimal, low-friction workout tracker designed specifically for people who suffer from gym anxiety, overthinking, and decision fatigue.**

Built with **Jetpack Compose**, **Material 3 Expressive**, **Android Health Connect**, and **GitHub Releases in-app updates**.

---

## 💡 Core Philosophy

- **Zero Friction / No Overwhelm:** Inside the gym, you should never have to think or choose. Routines run like a predefined script strictly capped at 3–4 movements.
- **Dark Mode First:** Deep OLED dark surfaces with restorative Sage Green and warm amber tones to reduce neurological stress and sensory overload.
- **Tunnel-Vision Mode:** In-workout view shows strictly **one exercise card at a time**, eliminating distracting lists, social feeds, and complex charts.
- **Unilateral Auto-Balance Engine:** Non-dominant arm (Left) is logged first; the dominant arm (Right) is capped automatically to preserve symmetry and stop over-development anxiety.
- **Form Tempo Guide:** A 2-second eccentric, 1-second pause, 1-second concentric visualizer guides lifting cadence.
- **1-Tap "Machine Occupied" Swap:** Instantly substitutes an occupied machine with a pre-configured drop-in equivalent without breaking flow.
- **Haptic Rest Timer:** Circular countdown with vibration alerts on completion so you can put your phone in your pocket.
- **Guilt-Free Early Exit:** 2 out of 4 exercises completed is celebrated as a 100% win. Consistency > perfection.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.3+
- **UI Toolkit:** Jetpack Compose with Material 3 Expressive tokens & animations
- **Architecture:** MVVM / Clean Architecture with StateFlow and Coroutines
- **Navigation:** Navigation 3 (`androidx.navigation3:navigation3-runtime`)
- **Health Connect:** Android Health Connect Client API (`androidx.health.connect:connect-client`)
- **Auto-Updates:** GitHub Releases in-app update manager with S3 redirect handling and secure `FileProvider` package installer
- **Build System:** Gradle 9.1 with Android Gradle Plugin (AGP) 9.0+

---

## 🚀 Building & Testing

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```
APK binary output: `app/build/outputs/apk/debug/app-debug.apk`
