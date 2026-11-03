# NNA Rules

Your Notifications, Your Rules. A small, lightweight, local-only automation tool for Android.

NNA Rules watches notifications and device state (charging, Wi-Fi, time schedules) to perform smart actions locally—100% private, with zero internet permission, no trackers, and no bloatware.

## Features

- **Triggers**:
  - **App Notifications**: Watch notifications from chosen apps, optionally filtering by keyword.
  - **Time Schedule**: Run rules daily at specific hours and minutes.
  - **Wi-Fi Network**: Trigger rules upon connecting to specific Wi-Fi SSIDs.
  - **Battery & Charger**: Trigger rules when connected to or disconnected from power.
- **Actions**:
  - **Clipboard & Sensitive OTP Copier**: Auto-extract OTPs (4–8 character standalone codes) or custom regex matches directly to your clipboard, mark clips as sensitive on Android 13+, and auto-clear after a configurable delay.
  - **Custom Notification & Alerts**: Show formatted notifications or play alert sounds through custom notification channels.
  - **Device Situation Controls**: Automatically adjust volume streams (Ring, Media, Alarm, Notification), screen brightness, and Do Not Disturb (DND) modes.
  - **Expense & Data Logger (CSV)**: Extract transaction amounts (e.g., banking/e-wallet SMS) via regex and append them into a local CSV file.
  - **Notification Digest**: Queue distracting notifications and deliver a batched summary at a scheduled daily hour.
- **Quick Presets & Recipes**: One-tap setup for common routines:
  - Auto-Copy OTP (with 30s auto-clear)
  - Bank / E-Wallet Expense Logger
  - Quiet Mode on Charger
  - Night Routine (22:00)
  - Office Wi-Fi Profile
- **Backup & Restore**: Easily Export and Import rules via portable JSON format.
- **Execution History**: Real-time activity log showing the last 30 executed rules with relative timestamps and outcomes.
- **Privacy First**: Everything stays strictly on the device. The app requests **no internet permission** (`INTERNET` is not declared in the manifest), includes zero analytics, and shows no ads.

## Architecture

Built with modern Android engineering practices:
- **`:core`**: Pure Kotlin JVM module containing the Rule Engine, Trigger/Action plugin registry, serialization codecs, and text extractors (no Android SDK dependencies, 100% unit-tested).
- **`:app`**: Android layer using Jetpack Compose, Material 3 with Dynamic Colors (Material You), Room Database with schema migrations, `NotificationListenerService`, Broadcast Receivers, and AlarmManager scheduling.

## Permissions

- **Notification access** (`BIND_NOTIFICATION_LISTENER_SERVICE`): Required to read notifications. Granted by the user in system settings.
- **Post Notifications** (`POST_NOTIFICATIONS`, Android 13+): Required to show custom notifications and digest summaries.
- **Do Not Disturb Access** (`ACCESS_NOTIFICATION_POLICY`): Required to adjust Do Not Disturb modes.
- **Modify System Settings** (`WRITE_SETTINGS`): Required for screen brightness adjustments.
- **Run at Startup** (`RECEIVE_BOOT_COMPLETED`): Reschedules daily time triggers and digest batches after device reboots.

## Build

Requirements: JDK 17 and Android SDK.

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

Debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy

All rules, notification text, and counters stay exclusively in the app's private storage on the device. Nothing is uploaded or transmitted.

## Author

NNazuna

## License

MIT License / TBD.
