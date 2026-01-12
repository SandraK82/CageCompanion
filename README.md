# CAGE Companion

A simple Android app for tracking Cannula Age (CAGE) with Nightscout integration.

## Features

- **CAGE Display**: Shows current cannula age in days:hours:minutes format
- **Color Coding**: Green (OK), Yellow (Warning), Red (Critical) based on configurable thresholds
- **Nightscout Integration**: Fetches CAGE from Nightscout and uploads new Site Change treatments
- **Home Screen Widget**: Compact widget showing current CAGE with color coding
- **Persistent Notification**: Always-visible status in notification shade with color
- **Automatic Updates**: App, widget, and notification update every minute
- **Multi-Language**: German, English, and French

## Screenshots

| Main Screen | Notification |
|-------------|--------------|
| Large circular CAGE display with color | Persistent notification with CAGE status |

## Installation

1. Download and install the APK from [Releases](../../releases)
2. Open Settings and configure your Nightscout URL and API Secret
3. Set your warning thresholds (default: Yellow at 2 days, Red at 2.5 days)
4. The app will automatically start tracking your CAGE

## Usage

### Main Screen
- Large circular display shows current CAGE with color coding
- **Refresh Icon**: Manually fetch latest CAGE from Nightscout
- **Settings Icon**: Open settings
- **Cannula Changed Button**: Record a new Site Change in Nightscout

### Widget
- Add the "CAGE Widget" to your home screen
- Shows CAGE with color-coded background
- Updates automatically every minute
- Tap to open the app

### Notification
- Persistent notification shows current CAGE
- Color-coded based on your thresholds
- Updates automatically every minute

## Configuration

### Nightscout Settings
- **URL**: Your Nightscout instance URL (e.g., `https://your-nightscout.herokuapp.com`)
- **API Secret**: Your Nightscout API secret or token for read/write access

### Threshold Settings
- **Yellow (Warning)**: Age at which display turns yellow (default: 2 days)
- **Red (Critical)**: Age at which display turns red (default: 2 days 12 hours)

## Requirements

- Android 8.0 (API 26) or higher
- Nightscout instance with API access

## Building from Source

```bash
git clone https://github.com/yourusername/CageCompanion.git
cd CageCompanion
./gradlew assembleDebug
```

## Tech Stack

- Kotlin
- Jetpack Compose
- Ktor HTTP Client
- DataStore Preferences
- Glance Widgets
- Foreground Service

## Privacy

- All data is stored locally on your device
- Only communicates with your personal Nightscout instance
- No analytics or tracking

## License

MIT License - see [LICENSE](LICENSE)

## Disclaimer

This app is not affiliated with Nightscout Foundation or any diabetes device manufacturer. It is an independent companion app for personal diabetes management. Always consult your healthcare provider for medical decisions.

---

Made with care for the diabetes community.
