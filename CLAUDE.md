# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kiosk Launcher is an Android app that turns Android devices into kiosk mode devices, locking them down to specific apps and preventing access to other system features. The app uses Android's Device Admin and Lock Task Mode APIs to achieve this functionality.

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
- **MainActivity**: Entry point that starts kiosk mode automatically
- **HomeFragment**: Main kiosk interface with app launcher and exit functionality
- **AppsListFragment**: Displays available apps that can be launched in kiosk mode
- **KioskUtil**: Core utility class containing kiosk mode start/stop logic
- **MyDeviceAdminReceiver**: Device admin receiver required for lock task permissions

### Key Directories
- `app/src/main/java/com/osamaalek/kiosklauncher/ui/`: UI components (Activities, Fragments)
- `app/src/main/java/com/osamaalek/kiosklauncher/util/`: Utility classes (KioskUtil, AppsUtil)
- `app/src/main/java/com/osamaalek/kiosklauncher/adapter/`: RecyclerView adapters
- `app/src/main/java/com/osamaalek/kiosklauncher/model/`: Data models

### Device Admin Requirements
The app requires Device Admin permissions and ideally Device Owner status to function fully. Key restrictions applied:
- Lock task mode activation (`startLockTask()`/`stopLockTask()`)
- App uninstall prevention (`DISALLOW_UNINSTALL_APPS`)
- Home app preference setting via `addPersistentPreferredActivity()`
- Lock task package whitelist management

## Target Configuration
- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 32
- **Compile SDK**: 32
- **Kotlin**: 1.6.21
- **Android Gradle Plugin**: 7.4.0-alpha07