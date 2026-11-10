# MobileSecure Android

A personal Android app starter that reads selected information from the phone where it is installed, after the owner grants Android permissions.

## Features
- Search/filter the local device call log by a number (blank means recent records).
- Display SIM state and limited operator/network information exposed by Android.
- Display the last known location of this device.
- Display basic device information.
- No backend, remote tracking, or data upload.

## Requirements
- Android Studio with JDK 17 support.
- Android SDK Platform 35.
- A physical Android phone is recommended for SIM, call-log and GPS testing.

## Run
1. Extract this ZIP.
2. Open Android Studio and select the extracted `MobileSecure` folder.
3. Allow Gradle sync. Install Android SDK 35 if prompted.
4. Connect your own Android phone with USB debugging enabled, or use an emulator for the basic UI.
5. Press Run.
6. Grant only the permissions you want to test. After granting a permission, tap the relevant feature again.

## Android limitations
- Call-log access is sensitive. Google Play restricts apps requesting `READ_CALL_LOG`; sideloaded personal testing may work, but device manufacturers or Android versions may still restrict access.
- Modern Android restricts subscriber phone numbers and SIM identifiers. This app displays only limited operator and SIM-state details available to ordinary apps.
- Location shows the last known location and may be blank until Android has a cached fix.
- A phone number alone cannot reveal another person's private call history, SIM records, or GPS location.
- This starter app keeps displayed information on-device and does not transmit it.

## Project
- Kotlin + Android Views
- Minimum Android 8.0 (API 26)
- Target SDK 35
- Package: `com.mobilesecure`

**Build status:** source uploaded; not yet verified with an Android SDK build.
