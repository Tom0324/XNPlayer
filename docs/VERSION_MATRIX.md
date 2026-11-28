# Version Matrix

本文件记录 XNPlayer 当前 APK 基线与其 native AAR 来源。旧版本信息以 Git 历史和 Release 页面为准。

---

## 当前 APK

| 项目 | 值 |
|---|---|
| versionName | `1.5.1` |
| versionCode | `10510` |
| APK versionCode（arm64-v8a split） | `105102` |
| ABI | `arm64-v8a` |
| 签名 | `CN=Trim`，v2 / v3 有效 |
| HW+ 默认 | `mediacodec,mediacodec-copy,no` |
| OpenList | 不内置 |

## 当前 native AAR

| 项目 | 值 |
|---|---|
| AAR Release | [`native-avcfix-mpv042-iso-eof-retained-frame-reset-20261129`](https://github.com/Tom0324/mpv-android-fongmi/releases/tag/native-avcfix-mpv042-iso-eof-retained-frame-reset-20261129) |
| Asset | `app-release.aar` |
| AAR SHA256 | `c5557fcfd46a5fc6403a93c1a5ce93b1aa4638f285848e3bd44b5cc4a02bf855` |
| 来源仓库 | [Tom0324/mpv-android-fongmi](https://github.com/Tom0324/mpv-android-fongmi) |
| 来源分支 | `iso-cache-fix-20261127` |
| ABI | 仅 `arm64-v8a` |

## 当前 native 修复链

- AVC PTS patch。
- `stream_iso/fongmi`、DVD / Blu-ray / ISO。
- disc demuxer 始终允许预读。
- JNI event 使用非空 `MPVNode`。
- FFmpeg MediaCodec configure retry：`20 × 100ms`。
- FFmpeg close diagnostics：`hw_buffer_count` 与 stop 决策。
- `hwdec_aimagereader.c` uninit 释放顺序修复。
- 同步 EOF frame reset：`vo_wakeup` + `VOCTRL_RESET` / `VOCTRL_FRAME_RESET`。
- EOF-only AImageReader retained-frame reset：`ra_hwdec_mapper_reset()` 在切集时释放 `pending_frame` / GL retained refs，正常播放仍保留 `retain_previous_frame` 防闪黑。

## 版本命名说明

native Release tag 中的 `mpv042` 是历史构建命名，不能单独作为二进制版本字符串依据。判断当前 native 基线应以 Release Notes、workflow pin 与 AAR SHA256 为准。

## JNI 接口

当前 AAR 保留 XNPlayer 所需的 mpvEx / MPVNode JNI 接口：

```text
event(int, MPVNode)
```
