# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kiosk Launcher is an Android app that turns Android devices into kiosk mode devices, locking them down to specific apps and preventing access to other system features. The app uses Android's Device Admin and Lock Task Mode APIs to achieve this functionality with modern transparent status bar support.

## Build System

This is a Gradle-based Android project using Kotlin:

- **Build**: `./gradlew build`
- **Clean**: `./gradlew clean`
- **Install Debug APK**: `./gradlew installDebug`
- **Run Tests**: `./gradlew test`
- **Run Instrumented Tests**: `./gradlew connectedAndroidTest`

## Architecture

The app follows a simple architecture with these key components:

### Core Structure
- **MainActivity**: Entry point that initializes default settings and starts kiosk mode automatically
- **HomeFragment**: Main kiosk interface with WebView and modern transparent status bar implementation
- **ConfigFragment**: Configuration interface with display settings management
- **AppsListFragment**: Displays available apps that can be launched in kiosk mode
- **DisplayUtil**: Manages fullscreen mode and status bar visibility settings
- **KioskUtil**: Core utility class containing kiosk mode start/stop logic
- **DebugLogger**: Logging utility for debugging display and status bar issues
- **MyDeviceAdminReceiver**: Device admin receiver required for lock task permissions

### Key Directories
- `app/src/main/java/com/osamaalek/kiosklauncher/ui/`: UI components (Activities, Fragments)
- `app/src/main/java/com/osamaalek/kiosklauncher/util/`: Utility classes (KioskUtil, DisplayUtil, DebugLogger)
- `app/src/main/java/com/osamaalek/kiosklauncher/adapter/`: RecyclerView adapters
- `app/src/main/java/com/osamaalek/kiosklauncher/model/`: Data models

### Display System Features
The app includes a sophisticated display management system:
- **Transparent Status Bar**: Modern implementation using AndroidX WindowInsets API
- **Fullscreen Mode**: Immersive mode with proper navigation bar handling for both portrait and landscape
- **Touch Preservation**: WebView touch interaction preserved in status bar area using layout margins
- **Safe Area Support**: CSS safe-area-inset injection for web content positioning
- **Automatic Configuration**: Optimal defaults applied on first launch (fullscreen=ON, hideStatusBar=OFF)

### Device Admin Requirements
The app requires Device Admin permissions and ideally Device Owner status to function fully. Key restrictions applied:
- Lock task mode activation (`startLockTask()`/`stopLockTask()`)
- App uninstall prevention (`DISALLOW_UNINSTALL_APPS`)
- Home app preference setting via `addPersistentPreferredActivity()`
- Lock task package whitelist management

## Recent Improvements (2025)

### Transparent Status Bar System
The app has been upgraded with a comprehensive transparent status bar implementation:

**Key Features**:
- Modern AndroidX WindowInsets API implementation
- Support for both portrait and landscape orientations
- Proper handling of status bar (top) and navigation bar (left/right/bottom) insets
- WebView touch interaction preserved in transparent areas
- CSS safe-area-inset variables injected for web content positioning

**Technical Implementation**:
- `WindowCompat.setDecorFitsSystemWindows(window, false)` in MainActivity
- `ViewCompat.setOnApplyWindowInsetsListener()` in HomeFragment
- `WindowInsetsCompat.Type.statusBars()` and `WindowInsetsCompat.Type.navigationBars()` for inset detection
- ConstraintLayout margins instead of View padding to preserve touch areas
- JavaScript injection for web content safe-area handling

**Display Settings**:
- **Fullscreen Mode**: Enabled by default - provides immersive experience
- **Hide Status Bar**: Disabled by default - allows transparent status bar effect
- Settings automatically initialized on first app launch
- Manual configuration available in ConfigFragment

### Build & Compatibility Updates
- Updated `compileSdk` from 32 to 33 for modern AndroidX compatibility
- Updated `androidx.core:core-ktx` from 1.7.0 to 1.9.0 for WindowInsets API
- Transparent status bar theme added to themes.xml
- All legacy window flag workarounds removed and replaced with modern approach

## Target Configuration
- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 32
- **Compile SDK**: 33 (Updated for androidx.core:core-ktx:1.9.0 compatibility)
- **Kotlin**: 1.6.21
- **Android Gradle Plugin**: 7.4.0-alpha07
- **AndroidX Core**: 1.9.0 (Required for modern WindowInsets API)

## Development Notes

### Display System
The transparent status bar implementation is designed for tablet use and works optimally with:
- Fullscreen mode enabled (immersive experience)
- Hide status bar disabled (transparent effect)
- These settings are automatically applied on first app launch

### Code Structure
- Modern WindowInsets handling in `HomeFragment.setupTransparentStatusBar()`
- Default settings initialization in `MainActivity.initializeDefaultSettings()`  
- Display settings management in `DisplayUtil`
- All complex workaround code has been removed in favor of clean AndroidX APIs

### Debugging
- `DebugLogger` provides comprehensive logging for display-related issues
- Status bar state, WebView state, and inset values are logged for troubleshooting
- JavaScript console logging available for web content positioning issues