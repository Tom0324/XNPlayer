# XNPlayer

[English](README_EN.md)

XNPlayer 是一个基于 mpvEx 的 Android 播放器项目，重点做了完整中文界面、扁平化 UI、mpv 0.42 + AVC 修复，以及一些针对网络播放的实用修正。

## 当前状态

最新 Release：

```text
https://github.com/Tom0324/mpvEx-private/releases
```

当前 APK 基线：

```text
应用名：XNPlayer
ABI：arm64-v8a
签名：CN=Trim
mpv：v0.42.0
FFmpeg：n9.1
libplacebo：v7.387.0
AVC：已打 PTS patch
OpenList：不内置
```

## 这个仓库负责什么

本仓库负责组装最终 XNPlayer APK：

```text
- mpvEx UI 壳
- Android App 模块
- 完整中文资源与硬编码汉化
- 扁平化 UI
- WebDAV 网络播放列表修复
- WebDAV 播放列表真实文件名显示
- 添加/编辑连接整页布局
- Tab 显示开关与默认启动页
- 本地 AVC-fixed mpv AAR
- arm64-v8a 单 ABI 构建
- 最终 APK Release
```

## native 内核仓库

native 内核不在这个仓库重建。  
用于生成 AVC-fixed mpv AAR 的仓库是：

```text
https://github.com/Tom0324/mpv-android-fongmi
```

该仓库负责：

```text
- mpv v0.42.0
- FFmpeg n9.1
- libplacebo v7.387.0
- AVC PTS patch
- 输出 mpv-android-avcfix.aar
```

## OpenList 策略

本项目不内置 OpenList。

OpenList 使用独立 OpenList-Mobile APK。  
如需在播放器中访问，请添加 WebDAV：

```text
http://127.0.0.1:5244/dav
```

## ABI

当前只构建：

```text
arm64-v8a
```

不构建：

```text
armeabi-v7a
x86
x86_64
universal
```

## 版本号说明

当前 APK 仍继承 mpvEx 的 `versionName = 1.3.1-1`、`versionCode = 1312`。  
后续建议切换为 XNPlayer 自己的版本体系：

```text
v1.0.5 → versionCode 10005, versionName 1.0.5
v1.1.0 → versionCode 10100, versionName 1.1.0
v2.0.0 → versionCode 20000, versionName 2.0.0
```

规则：

```text
versionCode = major * 10000 + minor * 100 + patch
```

注意：

```text
只要 applicationId 与签名不变，并且 versionCode 大于 1312，就可以覆盖安装。
versionName 显示成 1.0.5 不影响覆盖安装。
```

## 文档

- `docs/REPO_ROLES.md` - 仓库职责与不可删除项
- `docs/CUSTOM_CHANGES.md` - 所有本地自定义修改
- `docs/CONFLICTS.md` - 与上游 mpvEx 合并时的冲突点
- `docs/MAINTENANCE.md` - 维护与同步流程
- `docs/VERSION_MATRIX.md` - 版本矩阵
- `docs/OPENLIST.md` - OpenList 策略

## 构建

主 workflow：

```text
.github/workflows/ui-shell-unsigned.yml
```

流程：

```text
1. checkout master
2. 使用本地 app/libs/mpv-android-avcfix.aar
3. 只构建 arm64-v8a
4. 上传 unsigned APK 到 Release asset
```

最终签名 APK 会发布到 XNPlayer Release。

## 不要删除

```text
- app/libs/mpv-android-avcfix.aar
- .github/workflows/ui-shell-unsigned.yml
- docs/
- Release assets
- Tom0324/mpv-android-fongmi 仓库
```

## 致谢

XNPlayer 基于许多优秀开源项目和上游成果构建。这里列出主要来源与用途。

### 直接基础

- [mpvEx](https://github.com/marlboro-advance/mpvEx)  
  App UI 与 Android 工程基础。XNPlayer 的界面骨架来自 mpvEx。

- [mpvKt](https://github.com/mpvKt/mpvKt)  
  Kotlin/Android 播放器上游项目之一，为 Compose UI 与 mpv 集成提供基础。

- [mpv / mpv-android](https://github.com/mpv-player/mpv)  
  核心播放器 engine 与 Android JNI 基础。XNPlayer 使用 mpv `v0.42.0` 并保留 AVC PTS patch。

- [FFmpeg](https://github.com/FFmpeg/FFmpeg)  
  音视频解码与处理基础。当前 native AAR 使用 `n9.1`。

- [libplacebo](https://github.com/haasn/libplacebo)  
  高质量视频渲染基础。当前 native AAR 使用官方稳定 `v7.387.0`。

### Android / UI

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [AndroidX](https://developer.android.com/jetpack/androidx)
- [Kotlin](https://kotlinlang.org/)

### 本地 UI / 资源参考

- NeoPlayer 1.7.5 Beta3  
  部分中文字符串、颜色、主题、字体和界面布局参考来自用户提供的 Beta3 APK 反编译资源。当前项目只把这些作为本地 UI/文案参考，不等同于直接使用 Beta3 源码。

### 外部配套，不内置

- [OpenList-Mobile](https://github.com/OpenListTeam/OpenList-Mobile)  
  XNPlayer 不内置 OpenList。用户单独安装 OpenList-Mobile 后，可通过 WebDAV 地址访问：
  ```text
  http://127.0.0.1:5244/dav
  ```

- [OpenList](https://github.com/OpenListTeam/OpenList)  
  作为外部文件列表服务使用，不打包进 XNPlayer APK。

### 历史参考，当前主线不使用

- [AListLiteAndroid](https://github.com/Tom0324/AListLiteAndroid)  
  曾用于内置 OpenList 实验。当前方案不内置 OpenList，因此不进入主线。

完整致谢说明见：

```text
docs/ACKNOWLEDGEMENTS.md
```

