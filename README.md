# XNPlayer / mpvEx-private

Private-origin project now published as public for transparency and Actions quota reasons.

This is the main app repository. It assembles the final XNPlayer APK.

## What this repo is

This repo owns:

- mpvEx UI shell
- Android app module
- full Chinese localization
- app name `XNPlayer`
- network WebDAV playlist fixes
- real filename display for network playlist items
- About page / device info / software info
- local AAR dependency
- arm64-only APK build workflow
- final APK releases

This repo is **not** responsible for rebuilding the native mpv core.

Native core source is here:

```text
https://github.com/Tom0324/mpv-android-fongmi
```

## Current final app

Release:

```text
https://github.com/Tom0324/mpvEx-private/releases/tag/XNPlayer-v1.0.1-network-playlist-names
```

Latest APK features:

- app label: `XNPlayer`
- full Chinese UI except intentionally preserved brand/protocol names
- WebDAV network folder playlist support
- previous/next navigation for network playlist
- real filenames in network playlist and player title
- mpv `v0.42.0`
- AVC PTS patch
- FFmpeg `n9.1`
- libplacebo `v7.387.0`
- arm64-v8a only
- `CN=Trim` signed
- no built-in OpenList

## ABI

Current APK and AAR are:

```text
arm64-v8a only
```

No v7a, x86 or x86_64 build is currently produced.

## OpenList policy

OpenList is not built into this app.

Use the separately installed OpenList-Mobile APK and connect through WebDAV:

```text
http://127.0.0.1:5244/dav
```

See `docs/OPENLIST.md`.

## Documentation

- `docs/REPO_ROLES.md` - repository roles and do-not-delete list
- `docs/CUSTOM_CHANGES.md` - all local customizations
- `docs/CONFLICTS.md` - expected upstream merge conflicts and resolution policy
- `docs/MAINTENANCE.md` - maintenance and upstream sync procedure
- `docs/VERSION_MATRIX.md` - version targets
- `docs/OPENLIST.md` - OpenList policy

## Build

Main workflow:

```text
.github/workflows/ui-shell-unsigned.yml
```

It:

1. checks out `master`
2. uses local `app/libs/mpv-android-avcfix.aar`
3. builds only arm64
4. uploads unsigned APK as a Release asset

Final signed APKs are published manually under XNPlayer release tags.

## Important

Do not delete:

- `app/libs/mpv-android-avcfix.aar`
- `.github/workflows/ui-shell-unsigned.yml`
- `docs/` directory
- Release assets
- upstream reference links in docs
