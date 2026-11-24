# Conflict Map

This file lists all known customization points that can conflict with future `marlboro-advance/mpvEx` updates.

## 1. App identity

| File | Our change | Risk |
|---|---|---|
| `app/src/main/res/values/strings.xml` | `app_name = XNPlayer` | Upstream may change app name |
| `app/src/main/res/values-zh-rCN/strings.xml` | Chinese app name and translations | Upstream may overwrite translations |
| `AndroidManifest.xml` | label points to app_name | Low risk |
| signing config | final APK signed CN=Trim | Upstream release signing is different |

Resolution: our app identity wins. Keep `XNPlayer` and CN=Trim for our releases.

## 2. Native core dependency

| File | Our change | Risk |
|---|---|---|
| `app/build.gradle.kts` | local `files("libs/mpv-android-avcfix.aar")` | Upstream may use remote `libs.mpv.android` |
| `app/libs/mpv-android-avcfix.aar` | patched mpv 0.42 + FFmpeg n9.1 + lp v7.387 | Upstream may have different AAR |
| `gradle/libs.versions.toml` | upstream may update mpv dependency | Semantic conflict with local AAR |

Resolution: our local AAR wins unless a new native AAR is explicitly validated.

## 3. Localization

| File | Our change | Risk |
|---|---|---|
| `values/strings.xml` | merged Beta3 strings | Upstream may add/change keys |
| `values-zh-rCN/strings.xml` | 3655 Chinese strings | Upstream may add new English strings |
| Kotlin hardcoded strings | translated UI labels | Upstream may edit same UI files |
| `colors_beta3.xml` | Beta3-derived colors | Upstream may rename themes |
| `styles_beta3.xml` | Theme alignment | Upstream may change theme names |
| `roboto_medium_numbers.ttf` | player number font | Low risk |

Resolution: keep our Chinese UI. For upstream-added strings, translate manually.

## 4. About page and version display

| File | Our change | Risk |
|---|---|---|
| `AboutScreen.kt` | Beta3-like structure, GitHub link changed, donate removed | Upstream may add donate/update modules |
| `CrashActivity.kt` / device info | software/device info labels and full version strings | Upstream may change version source |

Resolution: keep device/software info, full FFmpeg commit, no donate.

## 5. Network playback fixes

| File | Our change | Risk |
|---|---|---|
| `NetworkBrowserScreen.kt` | passes current folder video list | Upstream may rewrite network browser |
| `NetworkBrowserViewModel.kt` | builds proxy playlist URIs and names | High conflict risk |
| `PlayerActivity.kt` | `playlistTitles`, real filename on next/prev | High conflict risk |
| `PlayerViewModel.kt` | playlist item title source | Upstream may rewrite metadata logic |
| `PlaylistSheet.kt` | title rendering | Upstream may change list/grid item model |

Resolution: preserve behavior, not exact patch lines:

```text
playlist contains playable URIs
playlist_names contains real filenames
playlist_index points to current item
title uses playlist_names[index] when available
```

## 6. Workflow and release

| File | Our change | Risk |
|---|---|---|
| `.github/workflows/ui-shell-unsigned.yml` | checkout master, arm64-only, Release asset upload | Upstream workflow changes |
| Actions artifact | removed due quota | Upstream may re-add artifact upload |
| Release tags | XNPlayer-v1.0 / v1.0.1 | No upstream equivalent |

Resolution: keep Release asset upload. Do not return to Actions artifact upload.

## 7. OpenList

| File/area | Our policy | Risk |
|---|---|---|
| all source | no built-in OpenList | Upstream may add OpenList code |
| `docs/OPENLIST.md` | external APK policy | Docs only |

Resolution: if upstream adds OpenList, remove or disable it in our branch unless user changes decision.

## 8. Documentation

| File | Our change | Risk |
|---|---|---|
| `README.md` | XNPlayer-specific intro | Upstream will overwrite on merge |
| `docs/REPO_ROLES.md` | repository roles | Upstream has no such docs |
| `docs/CUSTOM_CHANGES.md` | customization inventory | Upstream has no such docs |
| `docs/CONFLICTS.md` | conflict map | Upstream has no such docs |
| `docs/MAINTENANCE.md` | maintenance procedure | Upstream has no such docs |

Resolution: our docs always win in merges.

## 9. Semantic conflicts

These do not show as Git conflicts but must be checked:

```text
1. MPVLib JNI method set changes
2. event signature changes away from event(int, MPVNode)
3. FFmpeg version string changes
4. libplacebo version string changes
5. AVC PTS patch no longer applies
6. WebDAV proxy stops supporting Range/seek
7. upstream adds its own playlist system
8. upstream changes package/applicationId
9. upstream changes minSdk/targetSdk causing FGS or WebView behavior changes
```

## Merge rule

For conflicts:

```text
Identity, localization, native AAR, playlist fixes, docs: ours wins.
New upstream features with no conflict: take upstream.
New upstream features that touch our custom areas: merge manually and retest.
```

After any merge, run the full verification checklist in `docs/MAINTENANCE.md`.
