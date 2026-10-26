# 实现与实际验收记录

日期：2026-10-14（Asia/Shanghai）。本文件根据最后一次 Gradle 日志及 JUnit/device XML 实际生成，不把编译、JVM、设备结果混为一谈。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 目录与 Binding 记录 | [2026-10-17 MVVM 分包与 Binding 工厂](#2026-10-17-mvvm-分包与-binding-工厂) | 分层目录调整及 Binding 工厂回归 |
| 类注释与首批工具 | [2026-10-17 类注释与常用工具](#2026-10-17-类注释与常用工具) | 已有工具与类职责文档的验证记录 |
| 交付范围 | [已完成交付](#已完成交付) | 实现能力和已交付内容 |
| 构建配置 | [构建环境](#构建环境) | JDK、AGP、Gradle、SDK 与编译目标 |
| 构建与执行结果 | [最终命令及结果](#最终命令及结果) | 历史完整构建、单元测试和设备测试结果 |
| 测试分类 | [测试覆盖](#测试覆盖) | 网络、缓存、请求、Java、UI、Room 与设备测试 |
| SDK 与 IDE 兼容 | [SDK 36 调整与 IDE 运行诊断](#sdk-36-调整与-ide-运行诊断) | SDK 36 调整、Glide 兼容性与 IDE Run 复验 |
| 限制与待验证 | [当前限制与未验证项](#当前限制与未验证项) | 设备矩阵、主题、系统能力和发布边界 |
| 产物与报告 | [查看产物与报告](#查看产物与报告) | APK、测试与 Lint 报告路径 |
| 本次通用工具 | [2026-10-17 通用工具补齐](#2026-10-17-通用工具补齐) | 五个新增工具、41 项测试、Lint 与设备验证边界 |

## 2026-10-17 MVVM 分包与 Binding 工厂

- Sample 已建立 view/activity、view/fragment、view/adapter、viewmodel、model/api、model/entity、model/repository、worker 实际目录和 package；Photo 从 DemoApi 文件中拆出。Manifest、导航图、R8 keep、R/BuildConfig、测试及文档路径同步迁移。
- BaseActivity/BaseFragment 新增 BindingInflater 构造入口。四个 Sample 页面在公开无参构造中传 XxxBinding::inflate，统一由 Base 调用三参 inflate，attachToParent=false；保留原来的无参 Base 和 inflateBinding 重写入口。
- 新增 Java 工厂式 Activity/Fragment 重建测试；检查容器挂载、重建后生成新 Binding、旧视图与移除后的 Binding 释放。原手动重写的互操作测试保留，JavaInteropTest 7/7 通过；JVM XML 汇总 73/73、0 失败/错误/跳过。
- 完整命令 `:sample:assembleDebug :sample:assembleRelease :sample:assembleDebugAndroidTest testDebugUnitTest lintDebug --continue --console=plain` 已结束，退出码 0；Debug APK、Release R8/APK、androidTest APK 编译通过，Lint 0 Error/Fatal。本轮未重新执行设备自动化测试或 Android Studio Run；设备测试源码编译不代表真机行为验证。
- 目录与接入示例见 [MVVM](MVVM.md)，类职责索引和 Java 互操作说明已同步更新。

## 2026-10-17 类注释与常用工具

- 为全部 111 个命名源码类型补齐或完善 KDoc/Javadoc，包括 95 个生产类型及测试类型；说明用途、主要能力、生命周期/线程和调用边界。生产类型索引见 [CLASS_GUIDE](CLASS_GUIDE.md)。
- 新增 Texts、Dates、Numbers、Digests、ClipboardTools、SystemIntents 六类工具，保持 Java 静态入口；没有新增运行时第三方依赖，system 的本地测试新增 Robolectric 依赖。使用说明见 [TOOLS](TOOLS.md)。
- `:sample:assembleDebug testDebugUnitTest lintDebug --continue --console=plain` 在本机 JBR 21 下 BUILD SUCCESSFUL，退出码 0，耗时 2m 39s；首轮 70/70 JVM 测试通过、0 失败/错误/跳过，全部模块 Lint 0 Error/Fatal。
- 随后为系统启动新增两个边界测试，执行 `:core:system:testDebugUnitTest :core:system:lintDebug`，BUILD SUCCESSFUL，退出码 0，耗时 10s；system 工具测试 5/5 通过。最终所有模块 XML 汇总 72/72 JVM 测试通过、0 失败/错误/跳过，Lint 0 Error/Fatal。
- SDK 36、AGP 8.11.1、Gradle 8.13、Glide 5.0.5 保持不变。没有修改现有业务流程、自动运行系统分享或复制真实用户数据。
- 本轮未重新运行 Release R8、设备自动化测试或 Android Studio Run；历史验收保留在下文。系统工具的 Robolectric API 28 测试不代表真机的分享面板、浏览器或 Android 13+ 剪贴板预览表现。

## 已完成交付

- 7 个 Kotlin Android Library 模块 + Java/XML Sample，Version Catalog、Gradle Kotlin DSL、Wrapper、ProGuard consumer rules、GitHub Actions。
- 44 份 Kotlin Base 源文件；Sample 的 DemoActivity、DemoFragment、DemoAdapter、DemoApplication、ResultActivity、DetailsFragment 使用 Java，ViewModel/Repository/API/Worker 使用 Kotlin。
- Common 的 ApiResult/AppError/BusinessException 及 8 组分类常量。
- BaseRequest/BaseResponse/RequestState、NetworkClient/NetworkConfig、TokenProvider/TokenRefresher、ApiErrorMapper、JavaRequests/ResultCallback/CancelHandle、RetryPolicy、上传/下载 Progress 包装。
- Glide ImageLoader/ImageOptions/ImageCallback；MMKV AppPreferences、PreferencesCacheStore、MemoryCache、CachedRepository/CachePolicy、Room DemoDatabase/DemoDao/DemoRecord 与 1→2 迁移。
- LifecycleTasks/AppForegroundObserver；BaseActivity/BaseFragment/BaseDialogFragment/BaseViewModel、30 个公共颜色/27 个深色覆盖/Theme.BaseKit、LoadingTextView、PageStateView、LoadingDialog、UiFeedback/Clicks/ListLoadStateAdapter。
- Navigation/Permissions/NetworkStatus/Keyboard/Screen/AppFiles/DeviceInfo。
- [README](../README.md)、[架构](ARCHITECTURE.md)、[公共 API](API.md)、[依赖](DEPENDENCIES.md)、[Java 互操作](JAVA_INTEROP.md)、[请求 Base](REQUEST_BASE.md)、[公共颜色](COLORS.md)、[LoadingTextView](LOADING_TEXTVIEW.md)。

## 构建环境

| 项目 | 实际值 |
|---|---|
| 构建主机 | macOS / Apple Silicon |
| 当前构建运行 JDK | Android Studio JBR 21.0.8，Gradle Launcher/Daemon 均确认使用此 JDK |
| Java/Kotlin 字节码目标 | 17；实际检查 Java DemoActivity 和 Kotlin LoadingTextView 的 class major 均为 61 |
| Gradle | Wrapper 8.13 |
| AGP | 8.11.1 |
| Kotlin Android / KSP2 | 2.2.21 / 2.2.21-2.0.4；各模块显式 Kotlin Android 插件，ksp.useKSP2=true |
| SDK | Platform 36，Build Tools 35.0.0；minSdk 24、compile/targetSdk 36 |
| 设备 | 已连接 Pixel 3，Android 9 / API 28，arm64-v8a |

当前按用户选择使用 AGP 8.11.1 与本机 JBR 21，Gradle 同步调整为 8.13，Kotlin Android 为 2.2.21，Room 改为配套 KSP2 2.2.21-2.0.4。原 KSP 2.3.12 要求 AGP >=8.12，已替换。新增 LoadingTextView 也参与本次完整构建/测试，Java/Kotlin 字节码目标均实际验证为 17。MMKV AAR 包含 arm64-v8a、armeabi-v7a、x86、x86_64 四种 ABI。

当前按用户选择将全部 8 个模块的 compileSdk 和 Sample targetSdk 改为 36，保留 AGP 8.11.1、Gradle 8.13 与 JDK 21；CI 同步使用 platforms;android-36。本机已有该 SDK，无需卸载 android-36.1 或 android-37.0。

## 最终命令及结果

```sh
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew \
  :sample:assembleDebug :sample:assembleRelease \
  testDebugUnitTest lintDebug :sample:connectedDebugAndroidTest \
  --continue --console=plain
```

此前 SDK 37 / AGP 8.11.1 / JDK 21 命令 **BUILD SUCCESSFUL**，退出码 0，复验耗时 1m 1s；以下 59 项 JVM 和 4 项设备结果属于该次记录。SDK 36 的复验结果单独记录。其他机器配置自己的 JDK 21 路径即可，无需下载临时 JDK 17。

| 检查 | 结果 |
|---|---|
| Debug APK，含 Java UI 与 Kotlin Base 编译 | 通过 |
| Release APK，R8 混淆与收缩 | 通过；产物未配置正式签名 |
| JVM 单元测试 | 59/59 通过，0 失败，0 跳过 |
| Android 设备测试 | 4/4 通过，0 失败，0 跳过；当前 AGP/JDK 配置实际执行 |
| 所有模块 lintDebug | 当前配置通过，0 Error/Fatal；仍有下列非阻断 Warning |
| 实际安装/启动 DemoActivity | 成功，am start 返回 Status: ok |
| 源码尾部空白检查 | 通过 |

## 测试覆盖

| 测试集 | 数量 | 主要验证 |
|---|---|---|
| NetworkClientTest | 20 | suspend HTTP/JSON、HTTP/解析/业务/网络/超时错误、取消、重试上限、POST/one-shot 不重试、Header/Token、重定向凭证隔离、日志脱敏、并发单次刷新、刷新失败/重放上限、上传下载进度 |
| BaseResponseTest | 6 | 默认/自定义成功码、业务失败、缺少 code、缺少必需 data、允许无数据的成功接口 |
| MemoryCacheTest | 11 | TTL 边界、0 TTL、LRU、过期删除、scope 隔离、非法 TTL、时间溢出、三种缓存策略、取消、缓存写入异常 |
| BaseRequestTest | 7 | 请求状态、取消传播、旧请求无法覆盖新结果、Repository 失败/异常、生命周期销毁、完成后取消保留结果 |
| JavaInteropTest | 6 | Java 静态入口与构造函数、Java 继承 Kotlin Activity/Fragment、Binding 创建/释放、生命周期请求取消、Activity 销毁后清除 Glide Target、Java BaseRequest 响应解包/生命周期取消 |
| LoadingTextViewTest | 7 | 点击先进入 Loading、防重复点击、动态文字/加载文案、启用状态、Spannable/图标/间距/宽度恢复、手动模式与防抖、异常恢复、隐藏窗口暂停/移除清理 |
| RoomTest | 2 | 用户隔离/清理、SQLite v1→Room v2 数据保留和真实 schema 验证 |
| DeviceSmokeTest | 4 | Java Sample 的本地列表/Empty/Error/重试/Activity Result，MMKV native 持久化/用户退出隔离，持久缓存 TTL/设置 namespace 保护，LoadingTextView 点击/文字恢复/动画停止及重新挂载 |

此前新增 BaseResponse/BaseRequest 与 Java 请求调用测试 15 项全部通过。本次新增 LoadingTextView JVM 测试 7 项、真机测试 1 项，同样全部通过。Robolectric 的窗口为 GONE，JVM 测试验证非可见窗口暂停与移除清理；可见窗口的动画启动/停止/重新挂载由真实设备验证，动画断言尊重系统动画开关。颜色默认/night 资源完整性已检查；Sample 使用公共标题/正文色和 Theme.BaseKit。UI 真机复验曾因设备休眠和非安全锁屏暂停而失败，已在测试启动时唤醒并请求关闭非安全锁屏、对测试窗口添加 KEEP_SCREEN_ON，再次 3/3 通过；没有修改设备超时或安全设置。

设备测试最初发现 RecyclerView 在 Activity 已销毁时通过 Glide.with(view) 清理 Target 会崩溃；ImageLoader.clear 改用 Application RequestManager，Glide 委派给所属 manager 清理。修复后的完整命令及设备测试均通过，且增加 JVM 回归测试。

## SDK 36 调整与 IDE 运行诊断

此前 Android Studio Otter 3 Feature Drop 2025.2.3 同步提示 `Could not find compile target android-37`，涉及全部 8 个模块，运行 sample 报 `Please select Android SDK`。SDK Location 与 local.properties 均正确；本机实际安装目录包括 `android-36`、`android-36.1`、`android-37.0`。CLI 构建通过不代表 IDE 运行配置可用。

按用户确认保持 AGP 8.11.1，将 compile/targetSdk 调整为 36，CI 和依赖说明同步更新。项目使用已安装的 android-36，其他 SDK 保留。

Glide 同步从 5.0.9 改为 5.0.5：5.0.9 的 glide、gifdecoder、disklrucache 三个 AAR 要求 compileSdk >=37；实际检查 5.0.5 的三个 AAR metadata 均为 minCompileSdk=1，不会阻止 SDK 36。

本轮命令：

```sh
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew \
  :sample:checkDebugAarMetadata :sample:assembleDebug :sample:assembleRelease \
  testDebugUnitTest lintDebug --continue --console=plain
```

实际结果：BUILD SUCCESSFUL，退出码 0，耗时 3m 25s。AAR metadata 检查、Debug APK、Release R8、59/59 JVM 测试均通过，Lint 检查通过。此前 4 项设备自动化测试本轮未重新执行。

Android Studio 本轮 Gradle 同步完成，SDK 缺失提示消失；Run 控制台确认 sample 在 Google Pixel 7 上安装成功（11s 303ms），启动 DemoActivity 并连接应用进程。SDK 选择错误与 3 项 Glide AAR metadata 错误均已消除。

## 当前限制与未验证项

- Lint 仍提示部分依赖有更新版本；本次保持已确认可构建的稳定组合和 MMKV 的 32 位兼容 LTS。Sample 另有 dataExtractionRules、Overdraw、NotShrinkingResources、MissingApplicationIcon、ButtonStyle 等提示；都不是 Error/Fatal，没有关闭 Lint 或使用 baseline 隐藏错误。
- GitHub Actions 已配置构建与 API 28 x86_64 模拟器测试，但未创建远程仓库/推送，所以没有声称在线 CI 已运行。API 28 x86_64 CI 测试状态待首次远程执行。
- 当前只在 API 28 ARM64 真机验证；API 24/37、16 KB page-size、32 位和其它厂商设备尚未专项运行。
- 深色模式切换的专项真机检查、权限拒绝/设置页、IME/键盘、前后台切换、GIF/真实大文件传输和 WorkManager 周期调度尚未专项真机验证；对应代码已实现且参与编译。MockWebServer 验证 HTTP 行为，不能证明外部 JSONPlaceholder 一直可用。
- Release 未配置商业签名，也未发布 Maven artifact。账号 Token 刷新协议/敏感存储、正式数据库表及迁移、各业务错误码/文案由接入项目适配。
- Base 模块和 Sample 要求范围内的代码与文档已交付；以上是验证覆盖与接入边界。源码中没有 GlobalScope、主线程 Room 查询、destructive migration 或空值占位实现。

## 查看产物与报告

本地构建目录为 .gitignore 忽略内容，重新构建可再生成：

- [Debug APK](../sample/build/outputs/apk/debug/sample-debug.apk)
- [Release APK](../sample/build/outputs/apk/release/sample-release-unsigned.apk)
- [网络测试 HTML](../core/network/build/reports/tests/testDebugUnitTest/index.html)
- [缓存测试 HTML](../core/storage/build/reports/tests/testDebugUnitTest/index.html)
- [Java/Room 测试 HTML](../sample/build/reports/tests/testDebugUnitTest/index.html)
- [设备测试 HTML](../sample/build/reports/androidTests/connected/debug/index.html)
- [Sample Lint HTML](../sample/build/reports/lint-results-debug.html)

接入其他 App 的方法与初始化要求见 README 的“接入另一个工程”，各模块入口见 API.md。

## 2026-10-17 通用工具补齐

扩充 `TOOLS.md` 为分类索引、源码/调用参考、Java 示例、Pockly 能力迁移表与使用边界；同步 README 与 CLASS_GUIDE。新增 `Durations`、`ByteSizes`、`WebUrls`、`TextSpans`、`ViewLayouts`，没有增加依赖或变更构建配置。

实际执行：

```sh
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew \
  :core:common:testDebugUnitTest :sample:testDebugUnitTest \
  :core:common:lintDebug :core:ui:lintDebug --console=plain
```

结果：BUILD SUCCESSFUL，退出码 0，耗时 24s。

- common 完整 JVM 测试 14/14、sample 完整 JVM/Robolectric 测试 27/27 通过，0 失败、0 错误、0 跳过；其中本次新增 11 项（common 6、sample Java 5）。这些数字仅包含本轮两个测试任务，其他模块测试未重跑。
- 相关 Kotlin 与 Sample Java 源码、Java 测试编译通过，验证新增静态入口和默认重载可以从 Java 调用。
- common/ui Lint 均为 0 Error/Fatal；common 有 24 项构建工具/依赖版本更新提示，ui 无 Warning，没有为此更改既定版本。
- TOOLS 的 66 个本地链接全部可解析。新增代码与本次文档的尾部空白检查通过；当前仓库文件整体未跟踪，因此单独的 `git diff --check` 不能覆盖未跟踪文件，另对本次文件执行直接检查。
- 测试覆盖时长单位、小时边界与 Long 上限；IEC 文件大小单位、舍入与非法输入；URL 协议/主机/端口、UTF-8 编码、已有转义、fragment 与同名参数追加；文字 span 保留且不改原文；父容器布局参数与 RTL padding 保留；一次性预绘制及主动取消。

本次未执行设备测试、Release 打包或外部网络联通验证。Robolectric API 28 的结果不等同于设备上的 RTL 布局、系统栏、键盘、分享面板或 Android 13+ 剪贴板预览验证。

## 2026-10-26 原生权限 Manager 封装

实现改用本项目的原生 Activity Result API 流程，已移除第三方权限库依赖。

- `:core:system:testDebugUnitTest`：16 项测试通过，含 11 项权限状态机测试与 5 项原有系统工具测试。
- `:sample:testDebugUnitTest --tests dev.androidbasekit.sample.PermissionManagerTest`：9 项测试通过；覆盖已授权、弹框取消、并发、原生权限结果、设置页返回、View 销毁、Activity 重建、Manifest 校验和旧版通知无需申请。
- `:sample:assembleDebug`：通过。
- `:core:system:lintDebug`：0 errors，1 项既有 `SystemIntents.kt` 的 UseKtx 建议。
- `:core:system:dependencyInsight --dependency permissionx --configuration debugRuntimeClasspath`：无匹配依赖；源码与测试均无第三方权限库类型引用。
- `git diff --check`：通过。
- Robolectric 补充系统 CAMERA PermissionInfo，用原生 AlertDialog 与 Activity Result 分发验证入口；系统权限框、厂商永久拒绝行为仍需真机验证。
