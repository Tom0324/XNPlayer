# XNPlayer

[中文](README.md)

XNPlayer is an Android media player project based on mpvEx, focused on full Chinese localization, a flatter UI, mpv 0.42 with AVC fixes, and practical network playback improvements.

## Current status

Latest release:

```text
https://github.com/Tom0324/mpvEx-private/releases
```

Current APK baseline:

```text
App name: XNPlayer
ABI: arm64-v8a
Signature: CN=Trim
mpv: v0.42.0
FFmpeg: n9.1
libplacebo: v7.387.0
AVC: PTS patch applied
OpenList: not built in
```

## What this repo does

This repo assembles the final XNPlayer APK:

```text
- mpvEx UI shell
- Android app module
- full Chinese localization
- flatter UI
- WebDAV network playlist fixes
- real filenames in WebDAV playlists
- full-page add/edit connection screens
- tab visibility switches and default startup tab
- local AVC-fixed mpv AAR
- arm64-v8a-only build
- final APK releases
```

## Native core repo

The native core is rebuilt in:

```text
https://github.com/Tom0324/mpv-android-fongmi
```

It owns:

```text
- mpv v0.42.0
- FFmpeg n9.1
- libplacebo v7.387.0
- AVC PTS patch
- mpv-android-avcfix.aar
```

## OpenList policy

OpenList is not built into the app.

Use the standalone OpenList-Mobile APK and connect through WebDAV:

```text
http://127.0.0.1:5244/dav
```

## ABI

Only:

```text
arm64-v8a
```

No v7a, x86, x86_64 or universal builds.

## Versioning note

Current APK still inherits mpvEx values:

```text
versionName = 1.3.1-1
versionCode = 1312
```

Recommended XNPlayer versioning:

```text
v1.0.5 → versionCode 10005, versionName 1.0.5
v1.1.0 → versionCode 10100, versionName 1.1.0
v2.0.0 → versionCode 20000, versionName 2.0.0
```

Rule:

```text
versionCode = major * 10000 + minor * 100 + patch
```

As long as `applicationId` and signing stay the same, and `versionCode` is greater than 1312, the APK can overwrite-install.

## Docs

- `docs/REPO_ROLES.md`
- `docs/CUSTOM_CHANGES.md`
- `docs/CONFLICTS.md`
- `docs/MAINTENANCE.md`
- `docs/VERSION_MATRIX.md`
- `docs/OPENLIST.md`

## Build

Workflow:

```text
.github/workflows/ui-shell-unsigned.yml
```

It:

```text
1. checks out master
2. uses local app/libs/mpv-android-avcfix.aar
3. builds arm64-v8a only
4. uploads unsigned APK as a Release asset
```

Final signed APKs are published under XNPlayer releases.

## Acknowledgements

XNPlayer is built on top of several excellent open-source projects. The main sources and their roles are listed below.

### Direct foundation

- [mpvEx](https://github.com/marlboro-advance/mpvEx)  
  Base UI and Android project. The XNPlayer UI shell comes from mpvEx.

- [mpvKt](https://github.com/mpvKt/mpvKt)  
  Upstream Kotlin/Android player project that provided part of the Compose UI and mpv integration baseline.

- [mpv / mpv-android](https://github.com/mpv-player/mpv)  
  Core playback engine and Android JNI foundation. XNPlayer uses mpv `v0.42.0` with an AVC PTS patch.

- [FFmpeg](https://github.com/FFmpeg/FFmpeg)  
  Audio/video decoding and processing. The current native AAR uses `n9.1`.

- [libplacebo](https://github.com/haasn/libplacebo)  
  High-quality rendering library. The current native AAR uses stable `v7.387.0`.

### Android / UI

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [AndroidX](https://developer.android.com/jetpack/androidx)
- [Kotlin](https://kotlinlang.org/)

### Local UI / resource reference

- NeoPlayer 1.7.5 Beta3  
  Some Chinese strings, colors, themes, fonts and layout ideas were referenced from the user-provided Beta3 APK decompilation. They are used as local UI/text references, not as direct Beta3 source code.

### External companion, not bundled

- [OpenList-Mobile](https://github.com/OpenListTeam/OpenList-Mobile)  
  OpenList is not bundled into XNPlayer. Install OpenList-Mobile separately and connect through WebDAV:
  ```text
  http://127.0.0.1:5244/dav
  ```

- [OpenList](https://github.com/OpenListTeam/OpenList)  
  Used as an external file-list service, not packaged into the APK.

### Historical reference, not used in the current mainline

- [AListLiteAndroid](https://github.com/Tom0324/AListLiteAndroid)  
  Used only for earlier experiments with built-in OpenList. It is not part of the current mainline.

Full acknowledgement notes:

```text
docs/ACKNOWLEDGEMENTS.md
```

