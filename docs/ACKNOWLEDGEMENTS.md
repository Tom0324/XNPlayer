# Acknowledgements

XNPlayer would not be possible without the work of many open-source projects.

## Direct foundation

| Project | Role | Status in XNPlayer |
|---|---|---|
| [mpvEx](https://github.com/marlboro-advance/mpvEx) | Android UI and app shell | Direct base |
| [mpvKt](https://github.com/mpvKt/mpvKt) | Kotlin/Android player baseline | Upstream of mpvEx |
| [mpv](https://github.com/mpv-player/mpv) | Playback engine | v0.42.0 with AVC PTS patch |
| [mpv-android](https://github.com/mpv-player/mpv-android) | Android JNI/AAR structure and build approach | Build reference |
| [FFmpeg](https://github.com/FFmpeg/FFmpeg) | Audio/video decoding | n9.1 |
| [libplacebo](https://github.com/haasn/libplacebo) | Video rendering | v7.387.0 |

## Android and UI stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX

## Local UI / resource reference

- NeoPlayer 1.7.5 Beta3 was used as a local reference for some Chinese strings, colors, themes, fonts and layout ideas. It was not used as direct source code in the mainline build.

## External companion, not bundled

| Project | Role | Bundled? |
|---|---|---|
| [OpenList-Mobile](https://github.com/OpenListTeam/OpenList-Mobile) | External OpenList service app | No |
| [OpenList](https://github.com/OpenListTeam/OpenList) | External file list / WebDAV backend | No |

XNPlayer connects to OpenList through:

```text
http://127.0.0.1:5244/dav
```

## Historical references, not used in mainline

| Project | Note |
|---|---|
| [AListLiteAndroid](https://github.com/Tom0324/AListLiteAndroid) | Used only for earlier built-in OpenList experiments; not in current APK |
| [Tom0324/mpvEx](https://github.com/Tom0324/mpvEx) | Historical workspace containing localization and UI experiments; current clean mainline is `mpvEx-private` |
| [Tom0324/fongmi-mpv-android](https://github.com/Tom0324/fongmi-mpv-android) | Earlier native experiment; not used |
| [Tom0324/mpv-fongmi](https://github.com/Tom0324/mpv-fongmi) | Earlier FongMi AVC experiment; not used |

## Native build source used by this project

- `Tom0324/mpv-android-fongmi`  
  Maintained as the reproducible source of `mpv-android-avcfix.aar`.  
  It pins mpv, FFmpeg, libplacebo and applies the AVC PTS patch.

## Note

Including a project here does not mean it is bundled into the APK.  
The bundled components are the AVC-fixed mpv native AAR and the mpvEx-derived app shell.
