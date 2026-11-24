# Maintenance Guide

## Repository roles

| Repo | Role |
|---|---|
| `Tom0324/mpv-android-fongmi` | Native core / AAR source |
| `Tom0324/mpvEx-private` | App shell / localization / APK assembly |

## Branch strategy

In `Tom0324/mpvEx-private`:

```text
master              Our mainline. Always buildable. XNPlayer current version.
upstream-latest     Local branch that tracks marlboro-advance/mpvEx.
sync/YYYYMMDD       Temporary branch used to merge upstream into our work.
```

Never overwrite `master` directly with upstream.

## Add upstream remote

```bash
git remote add upstream https://github.com/marlboro-advance/mpvEx.git
git fetch upstream
```

If `upstream` already exists:

```bash
git remote set-url upstream https://github.com/marlboro-advance/mpvEx.git
git fetch upstream
```

## Upstream sync procedure

```bash
git fetch upstream
git checkout -b sync/20261124 upstream/master
git merge master
```

Resolve conflicts using `docs/CONFLICTS.md`.

Then verify:

```text
1. app_name is XNPlayer
2. local AAR dependency exists
3. workflow checks out master
4. workflow has no OpenList download
5. workflow uploads Release asset, not Actions artifact
6. playlist_names fix exists
7. network playlist fix exists
8. About page has no donate block
9. version strings are correct
10. only arm64 is built
```

Only after verification:

```bash
git checkout master
git merge --no-ff sync/20261124
```

Then build and test one arm64 APK.

## Native core update procedure

Only in `Tom0324/mpv-android-fongmi`:

```text
1. change mpv / FFmpeg / libplacebo pin if needed
2. keep AVC patch
3. run build_avcfix.yml
4. verify AAR version strings
5. verify JNI event(int, MPVNode)
6. publish a new AAR Release tag
```

Then in `Tom0324/mpvEx-private`:

```text
1. replace app/libs/mpv-android-avcfix.aar
2. update docs/VERSION_MATRIX.md
3. update About page version text
4. build one arm64 APK
5. sign and publish
```

## App release procedure

```text
1. run UI Shell Unsigned Build
2. download unsigned APK from Release asset
3. zipalign
4. sign with CN=Trim
5. verify cert and ABI
6. create XNPlayer release tag
7. attach signed APK
8. write Release notes explaining changes
```

## Rollback

If upstream merge breaks playback:

```text
1. checkout previous known-good master tag/commit
2. keep AAR unchanged
3. rebuild arm64 APK
```

Do not rollback by deleting docs or AAR.
