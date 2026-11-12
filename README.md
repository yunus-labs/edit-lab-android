# Edit Lab for Android

A Hinglish video-editing practice roadmap with Phone and PC tracks, 16 projects per track, and offline notes, ideas, and progress.

## Install

Download **Edit-Lab.apk** from [Releases](https://github.com/yunus-labs/edit-lab-android/releases/latest). Open it on Android 6.0 or later. Android may ask you to allow installation from the browser or file manager used to open the APK. Edit Lab uses Android System WebView and has no Internet permission. Progress is saved on the device.

## Build on Windows

Install JDK 17 or later, Android SDK Platform 36, and Build Tools 35.0.0. Set JAVA_HOME and ANDROID_HOME. Run the following from PowerShell:

    .\build-apk.ps1

This produces build/Edit-Lab-unsigned.apk. To produce an installable APK with your own signing key:

    .\build-apk.ps1 -SigningKey C:\path\to\key.p12 -KeyAlias edit-lab

Use the same signing key for future updates to an installed copy. The lesson content and web UI are in app/src/main/assets/index.html. The native WebView wrapper is in MainActivity.java.
