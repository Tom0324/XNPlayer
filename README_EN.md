# XNPlayer

[中文](README.md) · [Latest Release](https://github.com/Tom0324/XNPlayer/releases/latest) · [License](LICENSE)

> An arm64 Android video player built on the mpvEx app shell and a reproducible FongMi / NeoPlayer mpv native AAR. XNPlayer keeps HW+ as the default decoder path and provides a fully localized Chinese UI with WebDAV playback support.

---

## Overview

| Item | Value |
|---|---|
| Latest stable version | `v1.5.0` |
| Release tag | [`XNPlayer-v1.5.0-20261129-eof-frame-reset-sync`](https://github.com/Tom0324/XNPlayer/releases/tag/XNPlayer-v1.5.0-20261129-eof-frame-reset-sync) |
| App name | XNPlayer |
| Package | `app.marlboroadvance.mpvex` (`app.marlboroadvance.mpvex.preview` for preview builds) |
| `versionName` / `versionCode` | `1.5.1` / `10510` (APK reports `105102` after ABI split) |
| Architecture | `arm64-v8a` only |
| Signature | `CN=Trim`, v2 / v3 valid |
| Default HW+ order | `mediacodec,mediacodec-copy,no` |
| OpenList | Not bundled; external service via WebDAV |

## Features

### Playback core

- FongMi / NeoPlayer mpv native AAR with DVD / Blu-ray / ISO streaming support.
- HW+ (`mediacodec` direct surface) remains the default; no forced fallback to HW copy or software decoding.
- Includes the AVC PTS fix, MediaCodec configure retry, synchronous EOF frame reset, and FFmpeg close diagnostics.
- Preserves NeoPlayer features including Vulkan, Dolby Vision GPU mapping, HDR color mapping, stale-frame checks, retained frames, and AImageReader statistics.

### App layer

- Full Chinese localization and flat UI.
- Unified local and network playback lists.
- WebDAV browsing, recent playback, and auto-scroll to the last played item.
- Real filenames in WebDAV playlists instead of local proxy stream IDs.
- Playback position, intro / outro handling, and display-state restoration.
- Full-page add / edit connection screens for SMB / FTP / WebDAV.

### Intro / outro policy

`start=00:01:53` and `end=-00:45` in `mpv.conf` are intentional user settings. The app parses them dynamically and recalculates `start` for every file instead of hardcoding 113 seconds.

> With `hr-seek=no`, mpv seeks to the nearest keyframe before the target. Enable **Use precise seeking** in player settings if the intro skip must land exactly at 1:53.

---

## Build system

This repository assembles the app and final APK. The native AAR is built in a separate repository.

| Stage | Entry point | Description |
|---|---|---|
| Native AAR | [`Tom0324/mpv-android-fongmi`](https://github.com/Tom0324/mpv-android-fongmi) | Builds and publishes `mpv-android-avcfix.aar` |
| Unsigned APK | [`.github/workflows/ui-shell-unsigned.yml`](.github/workflows/ui-shell-unsigned.yml) | Cloud build with SHA256-verified AAR injection |
| Signed APK | [XNPlayer Releases](https://github.com/Tom0324/XNPlayer/releases) | Unsigned APK is signed locally with `CN=Trim` and published |

Workflow steps:

1. Check out source at the triggering ref.
2. Download the pinned AAR from the native Release and verify its SHA256.
3. Run `./gradlew --no-configuration-cache assembleStandardPreview`.
4. Upload the unsigned APK as a Release asset.
5. Zipalign, sign, verify, and publish the signed APK.

### Current native AAR baseline

| Item | Value |
|---|---|
| AAR Release | [`native-avcfix-mpv042-iso-eof-retained-frame-reset-20261129`](https://github.com/Tom0324/mpv-android-fongmi/releases/tag/native-avcfix-mpv042-iso-eof-retained-frame-reset-20261129) |
| AAR SHA256 | `c5557fcfd46a5fc6403a93c1a5ce93b1aa4638f285848e3bd44b5cc4a02bf855` |
| mpv string in AAR | `mpv v0.41.0-dev-g76f963cc4-dirty` |
| FFmpeg string in AAR | `Lavf/62.12.102` (release-8.1-fongmi derived) |
| libplacebo string in AAR | `v7.402.0` |

> Use the strings embedded in the AAR as the source of truth. The `mpv042` segment in the native Release tag is a historical build name.

---

## Verified dependencies and references

Only projects referenced by this README are listed, with their real status in the current build.

| Project | Link | Role | Used now? |
|---|---|---|---|
| XNPlayer | [Tom0324/XNPlayer](https://github.com/Tom0324/XNPlayer) | App source and releases | Yes, this repository |
| Native AAR source | [Tom0324/mpv-android-fongmi](https://github.com/Tom0324/mpv-android-fongmi) | Produces `mpv-android-avcfix.aar` | Yes, downloaded and verified by CI |
| mpvEx | [marlboro-advance/mpvEx](https://github.com/marlboro-advance/mpvEx) | UI / Android upstream baseline | Indirect, upstream reference |
| mpvKt | [abdallahmehiz/mpvKt](https://github.com/abdallahmehiz/mpvKt) | Kotlin / Compose upstream of mpvEx | Indirect, historical reference |
| mpv | [mpv-player/mpv](https://github.com/mpv-player/mpv) | Playback engine upstream | Reference, official build not compiled directly |
| mpv-android | [mpv-android/mpv-android](https://github.com/mpv-android/mpv-android) | Android JNI / AAR structure reference | Reference, not a direct dependency |
| FongMi/mpv | [FongMi/mpv](https://github.com/FongMi/mpv) | Kernel baseline, pinned at `76f963cc4` | Yes, via native AAR |
| NextPlayerCloud/mpv | [NextPlayerCloud/mpv](https://github.com/NextPlayerCloud/mpv) | `NeoPlayer` ref in native workflow | Yes, via native AAR |
| FFmpeg | [FFmpeg/FFmpeg](https://github.com/FFmpeg/FFmpeg) | Audio / video decoding upstream | Reference, official build not compiled directly |
| FongMi/FFmpeg | [FongMi/FFmpeg](https://github.com/FongMi/FFmpeg) | `release-8.1-fongmi` derived FFmpeg | Yes, via native AAR |
| NextPlayerCloud/FFmpeg | [NextPlayerCloud/FFmpeg](https://github.com/NextPlayerCloud/FFmpeg) | FFmpeg mirror in native workflow | Yes, via native AAR |
| libplacebo | [haasn/libplacebo](https://github.com/haasn/libplacebo) | High-quality rendering upstream | Reference; FongMi-derived build is used |
| OpenList | [OpenListTeam/OpenList](https://github.com/OpenListTeam/OpenList) | External file list / WebDAV backend | No, not bundled |
| OpenList-Mobile | [OpenListTeam/OpenList-Mobile](https://github.com/OpenListTeam/OpenList-Mobile) | Standalone Android service app | No, installed separately |

### Removed stale references

Older README revisions linked `Tom0324/AListLiteAndroid`, `Tom0324/fongmi-mpv-android`, and `Tom0324/mpv-fongmi`. These were early experiments, are not used by the current build, and are no longer accessible. They are intentionally not linked here.

### Android / UI stack

Kotlin, Jetpack Compose, Material 3, AndroidX, Room, Koin, OkHttp, Sardine, NanoHTTPD.

---

## OpenList policy

XNPlayer does not bundle OpenList, AListLib, or OpenList Service.

To access OpenList resources, install [OpenList-Mobile](https://github.com/OpenListTeam/OpenList-Mobile) separately and add this WebDAV URL in XNPlayer:

```text
http://127.0.0.1:5244/dav
```

See [docs/OPENLIST.md](docs/OPENLIST.md).

---

## Latest release and verification

| Item | Value |
|---|---|
| Release | [`XNPlayer-v1.5.0-20261129-eof-frame-reset-sync`](https://github.com/Tom0324/XNPlayer/releases/tag/XNPlayer-v1.5.0-20261129-eof-frame-reset-sync) |
| APK | `XNPlayer_v1.5.0_20261129_eofframe_reset_sync_arm64_CNTrim.apk` |
| Size | 32,392,097 B |
| SHA256 | `d42e5eabd8c866f8ce4468b10a1522e3fa9fe7811bf6c6e50743c15d3dfc1b6e` |
| Signature | `CN=Trim`, v2 / v3 valid |

Direct download: [XNPlayer v1.5.0 arm64 CNTrim APK](https://github.com/Tom0324/XNPlayer/releases/download/XNPlayer-v1.5.0-20261129-eof-frame-reset-sync/XNPlayer_v1.5.0_20261129_eofframe_reset_sync_arm64_CNTrim.apk)

## Installation notes

- arm64-v8a only.
- Overwrite installation requires the same `applicationId` and `CN=Trim` signature, with an increasing APK `versionCode`.
- If WebDAV recent-playback entries behave incorrectly after upgrading, remove the affected entry and play the file once again.

---

## Project principles

1. Keep HW+ default: `mediacodec,mediacodec-copy,no`; never switch to HW copy as the default.
2. Parse intro / outro values from `mpv.conf` dynamically; never hardcode seconds.
3. Build native components in GitHub Actions, not on local machines.
4. Keep OpenList external and decoupled from the player.
5. Keep release assets on GitHub Releases, not in short-lived Actions artifacts.

---

## Documentation

- [docs/OPENLIST.md](docs/OPENLIST.md) — OpenList integration policy
- [docs/CUSTOM_CHANGES.md](docs/CUSTOM_CHANGES.md) — local changes
- [docs/CONFLICTS.md](docs/CONFLICTS.md) — upstream merge conflicts
- [docs/RELEASE_HISTORY.md](docs/RELEASE_HISTORY.md) — historical release notes (v1.0–v1.1 archive)

## License

This project is licensed under the [Apache License 2.0](LICENSE). Third-party dependencies and upstream projects are governed by their own licenses.
