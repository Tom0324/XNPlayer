# Custom Changes

This file records all intentional differences from upstream `marlboro-advance/mpvEx`.

## App name

```text
app_name = XNPlayer
```

Files:

```text
app/src/main/res/values/strings.xml
app/src/main/res/values-zh-rCN/strings.xml
```

## Localization

- merged Beta3 strings
- merged Beta3 zh-rCN strings
- translated hardcoded Kotlin UI strings
- added Beta3 number font
- added Beta3 appwidget colors and mpvex theme resources

## Native core

- local AAR: `app/libs/mpv-android-avcfix.aar`
- mpv v0.42.0
- AVC PTS patch
- FFmpeg n9.1
- libplacebo v7.387.0

Source repo:

```text
https://github.com/Tom0324/mpv-android-fongmi
```

## About page

- Beta3-like structure
- device info / software info
- GitHub URL changed to our project
- donate block removed
- FFmpeg full commit displayed

## Network playlist

- current directory videos passed into player
- playlist URI list generated for WebDAV/SMB/FTP
- `playlist_names` carries real filenames
- `playlist_index` carries current item
- previous/next navigation enabled for network playlist
- real file title shown after switching items

## Build

- checkout `master`
- arm64-only
- no Actions artifact upload
- unsigned APK uploaded as Release asset
- final signed APK published under XNPlayer release tags

## OpenList

Not built in.

Use OpenList-Mobile APK separately with WebDAV:

```text
http://127.0.0.1:5244/dav
```

## Documentation

- repository roles
- maintenance guide
- conflict map
- version matrix
- OpenList policy
