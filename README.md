# XNPlayer

[English](README_EN.md) · [Latest Release](https://github.com/Tom0324/XNPlayer/releases/latest) · [License](LICENSE)

> 面向 arm64 Android 设备的本地 / 网络视频播放器。以 mpvEx 工程为 UI 基线，集成可复现的 FongMi / NeoPlayer mpv native AAR，保留 HW+ 默认解码策略，并提供完整的中文界面与 WebDAV 播放体验。

---

## 项目概览

| 项目 | 当前值 |
|---|---|
| 最新正式版本 | `v1.5.0` |
| 当前发布标签 | [`XNPlayer-v1.5.0-20261129-eof-frame-reset-sync`](https://github.com/Tom0324/XNPlayer/releases/tag/XNPlayer-v1.5.0-20261129-eof-frame-reset-sync) |
| 应用名称 | XNPlayer |
| 包名 | `app.marlboroadvance.mpvex`（preview 构建为 `app.marlboroadvance.mpvex.preview`） |
| `versionName` / `versionCode` | `1.5.1` / `10510`（ABI split 后 APK 显示 `105102`） |
| 架构 | 仅 `arm64-v8a` |
| 签名 | `CN=Trim`，v2 / v3 签名有效 |
| HW+ 默认解码顺序 | `mediacodec,mediacodec-copy,no` |
| OpenList | 不内置，作为独立服务通过 WebDAV 接入 |

## 核心能力

### 播放内核

- 基于 FongMi / NeoPlayer  recipe 的 mpv native AAR，支持 DVD / Blu-ray / ISO 流。
- 默认 HW+（`mediacodec` 直连 surface），不会被默认改成 HW copy 或软解。
- 已合入 AVC PTS 修复、MediaCodec configure 重试、EOF frame reset 同步化，以及 FFmpeg close 诊断。
- 保留 NeoPlayer 的 Vulkan / Dolby Vision GPU mapping / HDR 色彩空间映射 / stale-frame 检查 / retained frame / AImageReader 统计等特性。

### App 层

- 全中文界面与扁平化 UI。
- 本地视频与网络视频统一播放列表。
- WebDAV 目录浏览、最近播放、自动滚动到上次播放位置。
- WebDAV 播放列表显示真实文件名，而不是本地代理 stream id。
- 播放进度恢复、片头 / 片尾跳过、画面比例与缩放状态恢复。
- 添加 / 编辑连接使用整页布局，SMB / FTP / WebDAV 配置集中管理。

### 片头 / 片尾策略

`mpv.conf` 中的 `start=00:01:53` 与 `end=-00:45` 是有意保留的用户配置。App 会动态解析这两个值，并在每次加载视频时重新计算 `start`，不会写死 113 秒。

> 注意：`hr-seek=no` 时 mpv 的非精确 seek 会落到目标时间之前的关键帧。若希望片头跳过精确落在 1:53，可在播放器设置中开启“使用精确搜索”。

---

## 构建体系

本仓库只负责组装 App 与最终 APK；native AAR 不在此仓库重建。

| 阶段 | 入口 | 说明 |
|---|---|---|
| Native AAR | [`Tom0324/mpv-android-fongmi`](https://github.com/Tom0324/mpv-android-fongmi) | 编译并发布 `mpv-android-avcfix.aar` |
| Unsigned APK | [`.github/workflows/ui-shell-unsigned.yml`](.github/workflows/ui-shell-unsigned.yml) | GitHub Actions 云端构建，按 SHA256 校验并注入 AAR |
| 正式签名 APK | [XNPlayer Releases](https://github.com/Tom0324/XNPlayer/releases) | 下载 unsigned 包后本地 `CN=Trim` 签名并发布 |

云端 workflow 的关键步骤：

1. 按触发 ref 检出源码。
2. 从 native AAR Release 下载固定版本 AAR，并校验 SHA256。
3. 执行 `./gradlew --no-configuration-cache assembleStandardPreview`。
4. 将 unsigned APK 上传到 Release asset。
5. 本地完成 zipalign、签名、验证后发布正式 Release。

### 当前 native AAR 基线

| 项目 | 值 |
|---|---|
| AAR Release | [`native-avcfix-mpv042-iso-eof-retained-frame-reset-20261129`](https://github.com/Tom0324/mpv-android-fongmi/releases/tag/native-avcfix-mpv042-iso-eof-retained-frame-reset-20261129) |
| AAR SHA256 | `c5557fcfd46a5fc6403a93c1a5ce93b1aa4638f285848e3bd44b5cc4a02bf855` |
| mpv 实测字符串 | `mpv v0.41.0-dev-g76f963cc4-dirty` |
| FFmpeg 实测字符串 | `Lavf/62.12.102`（release-8.1-fongmi 派生） |
| libplacebo 实测字符串 | `v7.402.0` |

> 版本以 AAR 内实际字符串为准；native Release 名称中的 `mpv042` 是历史构建命名，不应当作二进制版本的唯一依据。

---

## 依赖与来源核实

下表只列出 README 涉及的项目，并明确标注其在当前构建中的真实状态。

| 项目 | 链接 | 用途 | 当前是否使用 |
|---|---|---|---|
| XNPlayer | [Tom0324/XNPlayer](https://github.com/Tom0324/XNPlayer) | App 源码与发布仓库 | 是，本仓库 |
| Native AAR 源码 | [Tom0324/mpv-android-fongmi](https://github.com/Tom0324/mpv-android-fongmi) | 产出 `mpv-android-avcfix.aar` | 是，CI 下载并校验 |
| mpvEx | [marlboro-advance/mpvEx](https://github.com/marlboro-advance/mpvEx) | UI / Android 工程上游基线 | 间接，作为上游参考 |
| mpvKt | [abdallahmehiz/mpvKt](https://github.com/abdallahmehiz/mpvKt) | mpvEx 的 Kotlin / Compose 上游 | 间接，历史参考 |
| mpv | [mpv-player/mpv](https://github.com/mpv-player/mpv) | 播放器引擎上游 | 参考，不直接编译官方版 |
| mpv-android | [mpv-android/mpv-android](https://github.com/mpv-android/mpv-android) | Android JNI / AAR 工程参考 | 参考，不直接依赖 |
| FongMi/mpv | [FongMi/mpv](https://github.com/FongMi/mpv) | 当前内核基线，固定提交 `76f963cc4` | 是，经 native AAR 间接使用 |
| NextPlayerCloud/mpv | [NextPlayerCloud/mpv](https://github.com/NextPlayerCloud/mpv) | native workflow 中 `NeoPlayer` ref 来源 | 是，经 native AAR 间接使用 |
| FFmpeg | [FFmpeg/FFmpeg](https://github.com/FFmpeg/FFmpeg) | 音视频解码上游 | 参考，不直接编译官方版 |
| FongMi/FFmpeg | [FongMi/FFmpeg](https://github.com/FongMi/FFmpeg) | FFmpeg `release-8.1-fongmi` 派生来源 | 是，经 native AAR 间接使用 |
| NextPlayerCloud/FFmpeg | [NextPlayerCloud/FFmpeg](https://github.com/NextPlayerCloud/FFmpeg) | native workflow 中 FFmpeg 镜像来源 | 是，经 native AAR 间接使用 |
| libplacebo | [haasn/libplacebo](https://github.com/haasn/libplacebo) | 高质量视频渲染上游 | 参考，实际使用 FongMi 派生版本 |
| OpenList | [OpenListTeam/OpenList](https://github.com/OpenListTeam/OpenList) | 外部文件列表 / WebDAV 后端 | 否，不打包进 APK |
| OpenList-Mobile | [OpenListTeam/OpenList-Mobile](https://github.com/OpenListTeam/OpenList-Mobile) | 独立 Android 服务 App | 否，需单独安装 |

### 已移除的过期引用

旧 README 曾列出 `Tom0324/AListLiteAndroid`、`Tom0324/fongmi-mpv-android`、`Tom0324/mpv-fongmi`。这些仓库属于早期实验，当前构建未使用，且已经不可访问，因此新版 README 不再链接。

### Android / UI 技术栈

Kotlin、Jetpack Compose、Material 3、AndroidX、Room、Koin、OkHttp、Sardine、NanoHTTPD。

---

## OpenList 策略

XNPlayer 不内置 OpenList，也没有内置 AListLib / OpenList Service。

如需在播放器中访问 OpenList 资源，请单独安装 [OpenList-Mobile](https://github.com/OpenListTeam/OpenList-Mobile)，然后在 XNPlayer 中添加 WebDAV 连接：

```text
http://127.0.0.1:5244/dav
```

详见 [docs/OPENLIST.md](docs/OPENLIST.md)。

---

## 最新发布与校验

| 项目 | 值 |
|---|---|
| Release | [`XNPlayer-v1.5.1-20261129-eof-retained-frame-reset`](https://github.com/Tom0324/XNPlayer/releases/tag/XNPlayer-v1.5.1-20261129-eof-retained-frame-reset) |
| APK | `XNPlayer_v1.5.1_20261129_eof_retained_frame_reset_arm64_CNTrim.apk` |
| 大小 | 32,392,097 B |
| SHA256 | `b40b39699876a70135decf8c24e7b22b3ecc6a70760f471ab34a636a5940c54f` |
| 签名 | `CN=Trim`，v2 / v3 有效 |

可直接下载：[XNPlayer v1.5.1 arm64 CNTrim APK](https://github.com/Tom0324/XNPlayer/releases/download/XNPlayer-v1.5.1-20261129-eof-retained-frame-reset/XNPlayer_v1.5.1_20261129_eof_retained_frame_reset_arm64_CNTrim.apk)

## 安装说明

- 仅支持 `arm64-v8a`。
- 覆盖安装需保持 `applicationId` 与 `CN=Trim` 签名一致，且 APK `versionCode` 递增。
- 从旧版本升级后，如果 WebDAV 最近播放记录异常，删除对应历史项并重新播放一次即可。

---

## 项目原则

1. HW+ 保持默认：`mediacodec,mediacodec-copy,no`，不为了规避偶发问题改成 HW copy 优先。
2. 片头 / 片尾时间从 `mpv.conf` 动态解析，不写死具体秒数。
3. native 构建走 GitHub Actions，不在本地编译 AAR 或 APK。
4. OpenList 不内置，保持播放器与服务解耦。
5. 发布资产保留在 GitHub Release，不依赖 Actions artifact 长期存储。

---

## 文档

- [docs/OPENLIST.md](docs/OPENLIST.md) — OpenList 接入策略
- [docs/CUSTOM_CHANGES.md](docs/CUSTOM_CHANGES.md) — 本地自定义修改记录
- [docs/CONFLICTS.md](docs/CONFLICTS.md) — 与上游合并时的冲突点
- [docs/RELEASE_HISTORY.md](docs/RELEASE_HISTORY.md) — 历史版本发布说明（v1.0–v1.1 归档）

## License

本项目使用 [Apache License 2.0](LICENSE)。第三方依赖与上游项目的许可条款以其各自仓库为准。
