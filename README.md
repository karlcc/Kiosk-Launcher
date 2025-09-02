# Kiosk Launcher

Kiosk Launcher is an Android app that transforms any Android device into a secure kiosk mode device. It provides enterprise-grade device lockdown with modern transparent status bar support and immersive fullscreen experience.

## Key Features

### Core Kiosk Functionality
- **Home Launcher Replacement**: Can be set as the default home app
- **Lock Task Mode**: Uses Android's native screen pinning for secure device lockdown
- **App Whitelist Management**: Control which apps can be launched in kiosk mode
- **Password Protection**: Secure exit mechanism with customizable passwords
- **Device Admin Integration**: Utilizes Android Device Admin APIs for enhanced security

### Modern Display System (2025 Update)
- **Transparent Status Bar**: Modern AndroidX WindowInsets implementation
- **Immersive Fullscreen**: Automatic fullscreen mode with proper navigation bar handling
- **Touch-Preserving Interface**: WebView interactions work seamlessly under transparent status bar
- **Orientation Support**: Optimized for both portrait and landscape modes on tablets
- **Auto-Configuration**: Optimal display settings applied automatically on first launch

### Technical Specifications
- **Target Devices**: Tablets (primary), phones (secondary)
- **Android Support**: Android 6.0 (API 23) and above
- **Modern Dependencies**: AndroidX Core 1.9.0, Compile SDK 33
- **WebView Integration**: Full web application support with CSS safe-area handling

## Quick Start

### Initial Setup
1. **Install the app** and grant necessary permissions
2. **Set as Home Launcher** (optional): Go to Android Settings > Apps > Default Apps > Home App
3. **Configure Display Settings**: App automatically applies optimal settings (fullscreen mode enabled)
4. **Set Custom URL**: Configure the web application URL in the config screen
5. **Enable Device Admin** (recommended): For enhanced security and lock task permissions

### Using Kiosk Mode
- **Start Kiosk**: App automatically enters kiosk mode on launch
- **Navigation**: Single tap anywhere to toggle the config button visibility
- **Web Content**: Full interaction with web applications under transparent status bar

### Exiting Kiosk Mode
1. **Single tap** anywhere on the screen to show the config button
2. **Tap the config button** (gear icon) in the top-left corner
3. **Enter password** when prompted
4. **Use config interface** to stop kiosk mode or adjust settings

## Configuration Options

### Display Settings
- **Fullscreen Mode**: Immersive experience with hidden navigation bars (enabled by default)
- **Hide Status Bar**: Traditional status bar hiding (disabled by default for transparent effect)
- **Transparent Status Bar**: Modern implementation allowing content under status bar with full touch interaction

### Security Settings
- **Custom Password**: Set secure password for exiting kiosk mode
- **Device Admin**: Enable for enhanced security and uninstall protection
- **Lock Task Whitelist**: Configure which apps can be launched in kiosk mode

### URL Configuration
- **Custom Web URL**: Set any web application as the kiosk content
- **Automatic Protocol**: App automatically adds HTTPS if not specified
- **WebView Settings**: Optimized for media playback and modern web applications

## Development & Technical Details

### Architecture
- **Modern Android**: Built with AndroidX libraries and modern Android practices
- **WebView Integration**: Full-featured web application support with touch preservation
- **Display Management**: Sophisticated system for handling status bars and fullscreen modes
- **Kiosk Security**: Device Admin integration for enterprise-grade lockdown

### Recent Updates (2025)
- Upgraded to modern AndroidX WindowInsets API
- Implemented transparent status bar with touch preservation
- Added automatic optimal settings configuration
- Enhanced tablet support with landscape navigation bar handling
- Removed legacy workaround code in favor of clean modern approaches

### Build Requirements
- Android Studio with Kotlin support
- Minimum SDK 23 (Android 6.0)
- Compile SDK 33
- AndroidX Core 1.9.0+

## Resources

### Original Articles
If you want to learn about the foundational design and technical approach:
- [Part 1: Building a Kiosk Launcher](https://medium.com/@osamaalek/how-to-build-a-kiosk-launcher-for-android-part-1-beb54476da56)
- [Part 2: Advanced Features](https://medium.com/@osamaalek/how-to-build-a-kiosk-launcher-for-android-part-2-9a529f503c11)

### License
Kiosk Launcher is licensed under the Apache License 2.0. See [LICENSE](https://github.com/osamaalek/Kiosk-Launcher/blob/master/LICENSE) for more details.

### Support
For questions, feedback, or suggestions:
- Open an issue on GitHub
- Contact: osamaalek@gmail.com
