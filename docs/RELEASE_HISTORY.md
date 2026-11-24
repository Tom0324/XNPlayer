# 发布历史

本文件保存 XNPlayer 各 Release 的完整说明。GitHub Release 页面仅保留简短摘要。
---

## XNPlayer-v1.0.5-version-ui-codecs

# XNPlayer v1.0.5

## 安装信息

```text
应用名：XNPlayer
包名：app.marlboroadvance.mpvex.preview
ABI：arm64-v8a
签名：CN=Trim
versionName：1.0.5
versionCode：100052
```

本次 versionCode 从旧的 `1312` 升级到 `100052`，可直接覆盖安装；安装器显示版本从 `1.3.1-1` 改为 `1.0.5`。

## 本次改动

### 1. 版本号体系改为 XNPlayer

```text
versionName：1.3.1 → 1.0.5
versionCode：131 → 10005
ABI 输出版本：1312 → 100052
preview 版本后缀：移除 commitCount
```

About 页里的应用名和版本文案也同步改为 XNPlayer 体系。

### 2. About 页设备编解码器支持

原“更新”区域替换为：

```text
设备编解码器支持
HDR10 / HDR10+ / HLG / 杜比视界
HEVC Main 10 / AVC High 10
```

该区域可点击进入详情页，查看系统完整编解码器列表，包括：

```text
编解码器名称
MIME
硬件加速 / 软件解码
最大支持分辨率
```

检测来源为 Android `MediaCodecList`，不访问网络，不影响播放链路。

### 3. 设备信息文案清理

修复版本信息显示为列表字符串的问题：

```text
去掉引号
去掉逗号
FFmpeg commit 单独一行
```

### 4. 播放列表面板透明化

播放列表面板外层不再使用不透明背景：

```text
surfaceColor = Color.Transparent
tonalElevation = 0.dp
```

现在面板背景透明，可透出视频画面。播放列表数据、切集逻辑、文件名显示逻辑未改动。

## 保持不变的内核版本

```text
mpv：v0.42.0
FFmpeg：n9.1 (74e4393595cf2b5857f0da925c4e0dd74ea306e5)
libplacebo：v7.387.0
AVC：PTS patch 已应用
OpenList：不内置
ABI：仅 arm64-v8a
```

## 验证结果

```text
zipalign：通过
apksigner verify：通过
签名：CN=Trim
application-label：XNPlayer
native-code：arm64-v8a
versionName：1.0.5
versionCode：100052
```

## Asset

```text
XNPlayer-v1.0.5-arm64-v8a-CNTrim.apk
SHA256：6d3021289373b3edfa23dd666a625b7f19124c618981c9a74c15e7e99519e946
```

完整说明见：

```text
docs/RELEASE_HISTORY.md
```


---

## XNPlayer-v1.0.4-connection-pages

# XNPlayer v1.0.4 发布说明

## 安装后应用名

```text
XNPlayer
```

## APK

```text
XNPlayer-v1.0.4-arm64-v8a-CNTrim.apk
```

```text
ABI：arm64-v8a
签名：CN=Trim
包名：app.marlboroadvance.mpvex.preview
```

---

## 本次更新

### 1. 添加/编辑连接改为整页布局

修复前：

```text
添加连接和编辑连接是悬浮窗口。
宽度受系统限制，字段显示不完整。
协议按钮被截断。
HTTPS、User-Agent 等区域不方便查看。
```

修复后：

```text
改为全屏页面式布局。
顶部有返回按钮。
内容区域可上下滚动。
底部固定取消 / 保存按钮。
```

字段按 Beta3 风格分组：

```text
协议：
  SMB / FTP / WebDAV

基础信息：
  名称 / 主机 IP 地址 / 端口 / 路径

认证：
  匿名访客访问 / 用户名 / 密码 / 密码显示切换

高级：
  使用 HTTPS / User-Agent
```

### 2. 网络卡片整张可点

修复前：

```text
只有链接图标可以进入连接。
点击卡片空白处没反应。
```

修复后：

```text
点击整张卡片任意空白区域都可以进入。
已连接时进入浏览。
未连接时发起连接。
链接图标仍然可用。
连接中时禁用整卡点击，避免重复触发。
```

三点菜单保留：

```text
连接 / 浏览
编辑
应用启动时自动连接
断开连接
删除
```

### 3. 不属于本次的变化

本次没有做：

```text
云朵图标
OpenList 内置
播放逻辑改动
mpv / FFmpeg / libplacebo 降级或重编
```

---

## 本版本包含的前序功能

```text
全中文 UI 基线
mpv v0.42.0
AVC PTS patch
FFmpeg n9.1
libplacebo v7.387.0
WebDAV 网络播放列表支持
WebDAV 播放列表真实文件名显示
扁平化 UI
Beta3 风格分集列表：
  无缩略图
  无时长占位
  无分辨率占位
Tab 显示开关
默认启动页
```

---

## OpenList 说明

本播放器不内置 OpenList。

请单独安装 OpenList-Mobile APK，然后通过 WebDAV 连接：

```text
http://127.0.0.1:5244/dav
```

---

## 验证结果

```text
zipalign：通过
apksigner verify：通过
签名：CN=Trim
应用名：XNPlayer
ABI：arm64-v8a
```

版本验证：

```text
mpv v0.42.0：通过
FFmpeg n9.1：通过
libplacebo v7.387.0：通过
```

---

## 回档点

```text
GitHub 备份分支：backup-before-connection-pages-20261124
本地源码备份：backup_before_connection_pages_20261124.tar.gz
```


---

## XNPlayer-v1.0.1-network-playlist-names

# XNPlayer v1.0.1 Release Notes

## 本次修复

修复 WebDAV 网络播放列表文件名显示错误。

### 问题

v1.0.0 中，网络页 WebDAV 播放列表显示的是 proxy stream id：

```text
1_1795493811393_0
1_1795493811393_1
```

点击下一部后，播放器顶部标题也显示同样的 stream id，而不是真实文件名。

原因：

```text
WebDAV 走本地 proxy。
播放器从 proxy URI 的 path 末段解析文件名。
path 末段是生成的 stream id，不是原始文件名。
```

### 修复

新增：

```text
playlist_names
```

网络播放时同时传递：

```text
playlist：proxy URI 列表
playlist_names：真实文件名列表
playlist_index：当前视频索引
```

播放器改为：

```text
1. 播放列表弹窗优先显示 playlist_names[index]
2. 切换上一/下一视频后，顶部标题优先显示 playlist_names[index]
3. 如果 playlist_names 不存在，仍回退到 URI 解析，兼容本地文件 / 普通网络流
```

涉及文件：

```text
NetworkBrowserViewModel.kt
PlayerActivity.kt
```

## 未改动 / 保持

```text
应用名：XNPlayer
mpv：v0.42.0
AVC PTS patch：保留
FFmpeg：n9.1 (74e4393595cf2b5857f0da925c4e0dd74ea306e5)
libplacebo：v7.387.0
ABI：arm64-v8a
签名：CN=Trim
OpenList：仍然不内置
```

## 静态验证

```text
application-label: XNPlayer
native-code: arm64-v8a
signer: CN=Trim
resources.arsc XNPlayer = 1
resources.arsc NeoPlayer = 0

dex playlist_names = 1
dex playlist_index = 1
dex network_file_path = 1

libmpv.so:
  mpv v0.42.0 = True
  n9.1 = True
  v7.387.0 = True
```

## 真机验证重点

1. 网络页 WebDAV 打开视频
2. 点击标题打开播放列表
3. 播放列表项应显示真实视频文件名
4. 点击下一部后顶部标题应显示真实视频文件名
5. 首页本地视频播放列表仍正常
6. AVC HW+ 仍同步


---

## XNPlayer-v1.0-network-playlist

# XNPlayer v1.0 Release Notes

## 安装后应用名

```text
XNPlayer
```

原 `NeoPlayer` 名称已从资源中移除。

## 本次修复

### 1. 网络页 WebDAV 播放列表

修复前：

```text
网络页通过 WebDAV 打开视频时，只传当前一个视频 URI。
播放器 playlist 为空。
点击标题没有“正在播放/播放列表”。
上一/下一按钮不显示。
```

修复后：

```text
网络页把当前目录内的视频列表一起传给 PlayerActivity。
传递 playlist 和 playlist_index。
当前视频对应正确索引。
点击标题可以打开播放列表。
启用上一个/下一个导航后按钮可用。
自动播放下一个视频也能按当前目录顺序工作。
```

涉及文件：

```text
NetworkBrowserScreen.kt
NetworkBrowserViewModel.kt
```

### 2. 内核版本

```text
mpv v0.42.0
AVC PTS patch 已保留
FFmpeg n9.1
libplacebo v7.387.0
```

FFmpeg 完整版本：

```text
n9.1 (74e4393595cf2b5857f0da925c4e0dd74ea306e5)
```

官方稳定版说明：

```text
FFmpeg n9.1
libplacebo v7.387.0
mpv v0.42.0
```

### 3. 内置 OpenList

```text
不内置 OpenList
```

OpenList 使用独立 OpenList-Mobile APK。  
如需在播放器访问，使用 WebDAV：

```text
http://127.0.0.1:5244/dav
```

静态检查结果：

```text
OpenList 管理 = 0
alistlib = 0
```

### 4. 仓库可见性

以下仓库已改为 public，避免 Actions artifact 配额问题：

```text
https://github.com/Tom0324/mpv-android-fongmi
https://github.com/Tom0324/XNPlayer
```

## APK 信息

```text
文件名：XNPlayer_v1.0_汉化_mpv042AVC_FFmpeg9.1_LP7.387_arm64_CNTrim.apk
ABI：arm64-v8a
签名：CN=Trim
包名：app.marlboroadvance.mpvex.preview
versionCode：1312
versionName：1.3.1-1
```

## 真机重点验证

1. 安装后图标下名称是否为 `XNPlayer`
2. 网络页 WebDAV 打开视频后：
   - 点击标题能打开播放列表
   - 上一/下一按钮能显示
   - 当前目录视频顺序正确
3. 首页本地视频播放列表仍正常
4. AVC HW+ 播放 2-5 分钟不漂移
5. OpenList-Mobile 独立 APK 正常时 WebDAV 地址可用


---

## ui-shell-arm64-ffmpeg9.1-lp7.387

Localization + mpv 0.42 AVC patch + FFmpeg n9.1 + libplacebo v7.387.0
---

## XNPlayer v1.0.9

# XNPlayer v1.0.9 发布说明

## 安装信息

```text
应用名：XNPlayer
包名：app.marlboroadvance.mpvex.preview
ABI：arm64-v8a
签名：CN=Trim
versionName：1.0.9
versionCode：100092
Asset：XNPlayer-v1.0.9-arm64-v8a-CNTrim.apk
SHA256：45816194f51a05e1c4eff185864457a0e33247b120878cb53041f4e3ece20a24
```

本次 `versionCode` 从 `100082` 升级到 `100092`，可直接覆盖安装 v1.0.8。

## 本次更新

### 1. WebDAV 播放写入最近播放

修复前，WebDAV 通过本地 proxy 播放，最近播放逻辑会过滤 `localhost / 127.0.0.1`，所以本地网络视频能显示、WebDAV 视频不显示。

现在改为保存稳定的：

```text
xnplayer-network://<connectionId>/<encoded original path>
```

实现效果：

```text
WebDAV 播放后写入最近播放
最近播放显示真实文件名
点击历史项时重建 proxy URL 并重新播放
```

注意：

```text
升级前已经播放过的旧 WebDAV 历史不会自动补回；
升级后重新播放一次的 WebDAV 文件会写入新的稳定历史。
```

### 2. 跟随视频方向修复

修复前，“跟随视频”读取 `video-params/aspect`，它可能被 Stretch / 自定义比例影响，导致 3840x1608 横屏 4K 视频被判成竖屏。

修复后新增源视频方向判断：

```text
video-params/w
video-params/h
video-params/rotate
```

效果：

```text
3840x1608，rotate=0 → 横屏
1608x3840，rotate=0 → 竖屏
带旋转元数据 → 按旋转后方向判断
```

不再受已保存的 Stretch / 自定义宽高比影响。

### 3. 版本号升级

```text
versionName：1.0.8 → 1.0.9
versionCode：10008 → 10009
APK versionCode：100082 → 100092
```

## 保持不变

```text
mpv：v0.42.0
FFmpeg：n9.1 (74e4393595cf2b5857f0da925c4e0dd74ea306e5)
libplacebo：v7.387.0
AVC PTS patch：保留
OpenList：不内置
ABI：仅 arm64-v8a
```

## 验证结果

```text
zipalign：通过
apksigner verify：通过
签名：CN=Trim
application-label：XNPlayer
native-code：arm64-v8a
versionName：1.0.9
versionCode：100092
```

内核版本：

```text
mpv v0.42.0 = True
FFmpeg n9.1 = True
libplacebo v7.387.0 = True
v7.399.0 = False
```

