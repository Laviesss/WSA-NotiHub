# WSA NotiHub

WSA NotiHub is a native Android application designed to act as the missing notification panel for Windows Subsystem for Android (WSA) users. It captures Android app notifications, stores them locally, and provides an interactive dashboard for managing them.

## Features

- **Notification Capture**: Automatically saves notifications from all installed Android apps.
- **Interactive Dashboard**: Reply, open, or dismiss notifications directly from the app.
- **Notification History**: Keep track of dismissed notifications in a local Room database.
- **OTP Detection**: Automatically identifies one-time passwords and provides a quick "Copy Code" button.
- **Floating Overlay Panel**: A draggable, compact view of recent notifications that stays on top.
- **Windows-Friendly Mode**: Optimized UI for mouse and keyboard usage with larger buttons and scroll support.
- **Notification API Bridge**: A local HTTP API (port 8765) to access notifications programmatically.
- **Automation Rules**: Set rules for auto-dismissing, auto-copying OTPs, or auto-opening apps.

## Setup Instructions for WSA

To use WSA NotiHub effectively on WSA, you need to grant specific permissions manually:

1. **Notification Access**:
   - The app will prompt you to open the "Notification Access" settings.
   - Find "WSA NotiHub" in the list and enable it.
   - This is required for the app to capture incoming notifications.

2. **Display Over Other Apps (Overlay Permission)**:
   - Required for the Floating Panel feature.
   - Go to App Settings > Advanced > Display over other apps and enable it for WSA NotiHub.

3. **WSA Networking**:
   - For the API Bridge to work, ensure WSA is configured with "Advanced networking" enabled if you wish to access it from Windows. Otherwise, it is accessible via `localhost:8765` within the WSA environment.

## Build Instructions

- Minimum SDK: 26 (Android 8.0)
- Target SDK: 36
- Build System: Gradle (Kotlin DSL)
- Language: Kotlin

To build the project, open it in Android Studio or run:
```bash
./gradlew assembleDebug
```

## API Bridge Usage

When enabled in Settings, you can fetch recent notifications via:
```bash
curl http://localhost:8765/notifications
```

## Pro Features

WSA NotiHub offers a Pro version available as a one-time in-app purchase.

- **Free Tier**: Notification dashboard, history, basic filtering, dismiss/open/reply actions, basic text search, app filter management.
- **Pro Tier (One-Time Unlock)**: Floating overlay panel, automation rules engine, local API bridge (port 8765), OTP auto-copy button.

Pro status is handled via Google Play Billing and can be restored if the app is reinstalled.

**Billing Mode:**
Billing is controlled by a single flag in `ProManager.kt`. By default it is disabled and the app runs fully free. To enable billing, set `BILLING_ENABLED = true` and rebuild. Billing requires Google Play Store distribution to function.

## Support the Project

If you find WSA NotiHub useful and would like to support its development, you can:

- [☕ Support on Ko-fi](https://ko-fi.com/laviesss)
- [⭐ GitHub Sponsors](https://github.com/sponsors/Laviesss)

Thank you for your support!

## License

MIT License - see the [LICENSE](LICENSE) file for details.
