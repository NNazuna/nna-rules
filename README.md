# NNA Rules

A small, local-only "if this, then that" automation app for Android.

Rules watch notifications from an app you choose and run one action when the
notification matches.

## Features

- **Trigger**: a notification from a chosen app, optionally containing a keyword.
- **Actions**:
  - show a custom notification with the original text,
  - play a sound through a notification channel,
  - copy the notification text to the clipboard.
- Rules can be enabled or disabled without deleting them.
- Each rule card shows how many times it matched and when it last matched.
- Dark theme support, including Android 12+ dynamic color.
- Everything stays on the device. The app has no internet permission and does
  not include analytics or ads.

## Screens

1. **First launch** explains notification access and links to the system screen
   where access is granted.
2. **Rules** lists saved rules as cards with an enable switch.
3. **Add / Edit rule** picks a trigger app, an optional keyword, an action, and
   whether the rule is enabled.

## How it works

The app uses a `NotificationListenerService` to read notifications after the
user grants notification access. Rules are stored locally with Room. When a
notification arrives, enabled rules for that package (and keyword, if set) are
matched, and the selected action runs.

To avoid loops, notifications posted by NNA Rules are ignored. Repeated updates
of the same notification are debounced.

## Permissions

- **Notification access** (`BIND_NOTIFICATION_LISTENER_SERVICE`): required to
  read notifications. Granted by the user in system settings.
- **Notifications** (`POST_NOTIFICATIONS`, Android 13+): required to show the
  custom notification and sound actions.

Both are requested in the app. No other permissions are used.

## Build

Requirements: JDK 17 and Android Studio (or an Android SDK with build tools).

Open the project in Android Studio and run the `app` configuration on a device
running Android 7.0 (API 24) or newer, or use:

```
./gradlew assembleDebug
```

## Known limitations

- The app must not be battery-restricted by the device vendor, or the listener
  may be stopped in the background.
- Notifications with hidden or missing content (for example on a locked screen)
  cannot be matched.
- Sound is played through a notification channel, so the device's notification
  volume and Do Not Disturb rules apply.
- Notification access is revoked when the app is uninstalled and must be granted
  again after reinstalling.

## Privacy

All rules, captured notification text, and match counters stay in the app's
private storage on the device. Nothing is uploaded.

## Author

NNazuna

## License

Not decided yet.
