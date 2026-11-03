# NNA Rules

A small, local-only "if this, then that" automation app for Android.

Rules watch notifications from an app you choose and run one action when the
notification matches.

## Features

- **Trigger**: a notification from a chosen app, optionally containing a keyword.
- **Actions**:
  - show a custom notification with the original text,
  - play a sound through a notification channel,
  - copy the notification text to the clipboard, either in full or after
    extracting a code (OTP preset or custom regex).
- Clipboard rules can mark copied clips as sensitive on Android 13+ and
  optionally clear the clipboard after a delay.
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
   whether the rule is enabled. Clipboard rules also get extraction settings and
   a sample-text preview.

## How it works

The app uses a `NotificationListenerService` to read notifications after the
user grants notification access. Rules are stored locally with Room. When a
notification arrives, enabled rules for that package (and keyword, if set) are
matched, and the selected action runs.

To avoid loops, notifications posted by NNA Rules are ignored. Repeated updates
of the same notification are debounced.

For clipboard rules, the copied text can be reduced to a code first. The OTP
preset takes the first standalone 4–8 character code that contains a digit.
A custom regular expression uses its first capture group, or the whole match
when the pattern has no group. If no code is found, the full text is copied.
The edit screen previews the result before saving. Copied clips are flagged as
sensitive on Android 13+, and a per-rule timer can clear the clipboard after a
delay.

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
- Clipboard clear timers stop if the app process is killed. Android blocks
  clipboard reads in the background, so a scheduled clear is unconditional and
  may also clear something copied later.
- Custom regular expressions run without a timeout on notification text.
- The OTP preset only recognizes ASCII letters and digits.

## Privacy

All rules, captured notification text, and match counters stay in the app's
private storage on the device. Nothing is uploaded.

## Author

NNazuna

## License

Not decided yet.
