# AndroidBaseKit 常用工具类

> 整理日期：2026-10-17。依据当前仓库源码、Sample 与测试调用点整理。包含基础工具、框架辅助入口，以及与 Pockly 工具的对应关系。示例为 Java 调用片段，需放入已有页面、Repository 或后台任务上下文，并按需导入类型。

## 1. 模块与选用原则

| 模块 | 复用能力 | 包名 |
| --- | --- | --- |
| `core:common` | 文本、日期、数字、时长、字节大小、URL 与摘要 | `dev.androidbasekit.common` |
| `core:ui` | 点击、提示、文字着色、View 布局、页面与列表状态 | `dev.androidbasekit.ui` |
| `core:image` | Glide 图片加载、圆角、头像、GIF、取消和缓存清理 | `dev.androidbasekit.image` |
| `core:system` | 屏幕、键盘、网络状态、文件目录、权限、导航与系统 Intent | `dev.androidbasekit.system` |
| `core:storage` | MMKV 设置、内存与持久缓存、Repository 缓存策略 | `dev.androidbasekit.storage` |
| `core:lifecycle` | 生命周期后台任务、应用前后台观察 | `dev.androidbasekit.lifecycle` |
| `core:network` | Retrofit、请求生命周期、错误转换、传输进度 | `dev.androidbasekit.network` |

- `core` 不依赖 Sample 或任何产品业务；按需引入模块，不复制 Pockly 的包名、资源、用户模型和全局 Application。
- 工具对象使用 `@JvmStatic` 暴露 Java 静态入口；有默认参数的新增工具提供 Java 重载。缓存、请求和观察器是实例，不能全部视为静态工具。
- 下表的“调用参考”区分正式 Sample 使用、测试使用和暂无调用。测试参考表示可查阅行为，不代表已接入产品或完成设备验证。
- 本次补齐 `Durations`、`ByteSizes`、`WebUrls`、`TextSpans`、`ViewLayouts` 五个通用工具；已有能力沿用原入口。

## 2. 基础文本、数字与摘要

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [Texts](../core/common/src/main/kotlin/dev/androidbasekit/common/Texts.kt) | 空白判断、回退、Unicode 码点截断 | `isBlank()`、`orDefault()`、`truncate()` | [CommonToolsTest（测试）](../core/common/src/test/kotlin/dev/androidbasekit/common/CommonToolsTest.kt) |
| [Numbers](../core/common/src/main/kotlin/dev/androidbasekit/common/Numbers.kt) | 精确舍入、本地化分组显示 | `decimal()`、`grouped()` | [CommonToolsTest（测试）](../core/common/src/test/kotlin/dev/androidbasekit/common/CommonToolsTest.kt) |
| [Digests](../core/common/src/main/kotlin/dev/androidbasekit/common/Digests.kt) | 文本、字节、流和文件 SHA-256 | `sha256()` | [CommonToolsTest（测试）](../core/common/src/test/kotlin/dev/androidbasekit/common/CommonToolsTest.kt) |
| [TextSpans](../core/ui/src/main/kotlin/dev/androidbasekit/ui/TextSpans.kt) | 区间、关键词文字着色，保留原有样式 | `colorRange()`、`colorMatches()` | [PortableUiToolsTest（测试）](../sample/src/test/java/dev/androidbasekit/sample/PortableUiToolsTest.java) |

集合判空、安全解析数字、字符串比较不额外叠加工具层：Java 使用 `items == null || items.isEmpty()`、`Objects.equals(a, b)`、`Integer.parseInt()` 配合 `NumberFormatException`；Kotlin 使用 `isNullOrEmpty()`、`toIntOrNull()`。积分、会员奖励、视频 ID 解析属于接入 App。

## 3. 日期、时长与文件大小

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [Dates](../core/common/src/main/kotlin/dev/androidbasekit/common/Dates.kt) | 毫秒时间戳与日期字符串互转 | `format()`、`parse()` | [CommonToolsTest（测试）](../core/common/src/test/kotlin/dev/androidbasekit/common/CommonToolsTest.kt) |
| [Durations](../core/common/src/main/kotlin/dev/androidbasekit/common/Durations.kt) | 通用时长显示，自动选择 `mm:ss` / `H:mm:ss` | `formatSeconds()`、`formatMillis()` | [PortableToolsTest（测试）](../core/common/src/test/kotlin/dev/androidbasekit/common/PortableToolsTest.kt) |
| [ByteSizes](../core/common/src/main/kotlin/dev/androidbasekit/common/ByteSizes.kt) | 1024 进制文件大小显示 | `format()` | [PortableToolsTest（测试）](../core/common/src/test/kotlin/dev/androidbasekit/common/PortableToolsTest.kt) |

`Dates` 处理日期，`Durations` 处理经过的时长；时长不经过时区，不在 24 小时回绕。`TimeConstants` 是毫秒常量，不是日期格式化工具。调度、倒计时和重试由生命周期/网络层负责。

## 4. 资源与界面

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [ViewLayouts](../core/ui/src/main/kotlin/dev/androidbasekit/ui/ViewLayouts.kt) | 动态尺寸、相对 margin、底部 padding、一次性预绘制 | `setSize()`、`setMargins()`、`setBottomPadding()`、`onNextPreDraw()` | [PortableUiToolsTest（测试）](../sample/src/test/java/dev/androidbasekit/sample/PortableUiToolsTest.java) |
| [Clicks](../core/ui/src/main/kotlin/dev/androidbasekit/ui/UiFeedback.kt) | 每个监听器独立的点击间隔过滤 | `setDebounced()` | [DemoFragment](../sample/src/main/java/dev/androidbasekit/sample/view/fragment/DemoFragment.java) |
| [UiFeedback](../core/ui/src/main/kotlin/dev/androidbasekit/ui/UiFeedback.kt) | 字符串资源 Toast、Snackbar | `toast()`、`snackbar()` | [DemoFragment](../sample/src/main/java/dev/androidbasekit/sample/view/fragment/DemoFragment.java) |
| [Screen](../core/system/src/main/kotlin/dev/androidbasekit/system/Screen.kt) | dp/sp 转 px、布局后的窗口边界 | `dp()`、`sp()`、`windowBounds()` | 暂无 Sample 直接调用 |

资源直接使用当前 Context 的 `getString()`、`getColor()`、`getResources().getDimensionPixelSize()` 和 `getQuantityString()`。不引入全局资源单例，避免多语言、深色模式、窗口 Context 更新后仍读取旧资源。尺寸优先复用接入 App 已有 dimen。

页面工具另见 [CLASS_GUIDE](CLASS_GUIDE.md)：`PageStateView.render()`、`ListLoadStateAdapter`、`LoadingDialog`、`LoadingTextView`。它们管理页面或控件状态，不负责发网络请求。动画可直接使用 `View.animate()`，页面负责取消。

## 5. 图片与缓存

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [ImageLoader](../core/image/src/main/kotlin/dev/androidbasekit/image/ImageLoader.kt) | 普通图、头像、圆角、GIF、预加载、取消与清缓存 | `load()`、`loadAvatar()`、`loadRounded()`、`preload()`、`clear()`、`clearMemory()`、`clearDiskAsync()` | [DemoAdapter](../sample/src/main/java/dev/androidbasekit/sample/view/adapter/DemoAdapter.java) |

GIF 使用 `ImageOptions(gif = true)`；Java 可使用完整构造参数或直接使用 Glide 的复杂请求能力。`loadRounded()` 的 radius 是 dp，布局工具使用 px。内存清理在主线程，磁盘清理在后台；`clearDiskAsync()` 的 executor 必须由调用方提供后台线程。

封面取色后的渐变配色、音乐/歌单默认背景没有搬入基础库；这部分需要产品提供颜色策略和资源。

## 6. 系统、键盘与权限

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [Keyboard](../core/system/src/main/kotlin/dev/androidbasekit/system/Keyboard.kt) | IME 显隐 | `show()`、`hide()` | 暂无 Sample 直接调用 |
| [Permissions](../core/system/src/main/kotlin/dev/androidbasekit/system/Permissions.kt) | 查询与注册权限申请 launcher | `isGranted()`、`register()` | 暂无 Sample 直接调用 |
| [Navigation](../core/system/src/main/kotlin/dev/androidbasekit/system/Navigation.kt) | 显式 Activity Intent、注册结果回调 | `intent()`、`registerResult()` | 暂无 Sample 直接调用；Sample 直接使用官方 API |
| [DeviceInfo](../core/system/src/main/kotlin/dev/androidbasekit/system/DeviceInfo.kt) | 应用版本、SDK、型号、厂商 | `appVersion()`、`sdk()`、`model()`、`manufacturer()` | 暂无 Sample 直接调用 |
| [ClipboardTools](../core/system/src/main/kotlin/dev/androidbasekit/system/ClipboardTools.kt) | 用户主动复制文本、敏感预览标记 | `copyText()` | [SystemToolsTest（测试）](../core/system/src/test/kotlin/dev/androidbasekit/system/SystemToolsTest.kt) |
| [SystemIntents](../core/system/src/main/kotlin/dev/androidbasekit/system/SystemIntents.kt) | 浏览器、文本分享、应用设置 Intent 和启动 | `web()`、`shareText()`、`appSettings()`、`startIfAvailable()` | [SystemToolsTest（测试）](../core/system/src/test/kotlin/dev/androidbasekit/system/SystemToolsTest.kt) |

权限注册应早于 STARTED；何时申请、拒绝后的文案由业务页面决定。Keyboard 要求带 Activity Context 的 View，在主线程使用。系统栏优先使用 `WindowCompat` / `ViewCompat` / `WindowInsetsCompat`；当前没有统一 edge-to-edge 包装器，接入 App 的 Base 页面应明确 Insets 消费与 padding 规则。

## 7. 存储、文件与序列化

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [AppPreferences](../core/storage/src/main/kotlin/dev/androidbasekit/storage/AppPreferences.kt) | MMKV 应用/用户设置 | `initialize()`、`open()`、`putString()`、`getString()`、`putInt()`、`getInt()`、`remove()`、`clearUserData()` | [DemoApplication](../sample/src/main/java/dev/androidbasekit/sample/DemoApplication.java)、[DemoRepository](../sample/src/main/kotlin/dev/androidbasekit/sample/model/repository/DemoRepository.kt) |
| [MemoryCache](../core/storage/src/main/kotlin/dev/androidbasekit/storage/MemoryCache.kt) | 有容量限制的线程安全 LRU/TTL 缓存 | `get()`、`put()`、`remove()`、`clear()`、`deleteExpired()` | [DemoRepository](../sample/src/main/kotlin/dev/androidbasekit/sample/model/repository/DemoRepository.kt) |
| [PreferencesCacheStore](../core/storage/src/main/kotlin/dev/androidbasekit/storage/PreferencesCacheStore.kt) | 用户隔离、带 TTL 的 MMKV 字符串缓存 | `get()`、`put()`、`remove()`、`clear()`、`deleteExpired()` | [DemoRepository](../sample/src/main/kotlin/dev/androidbasekit/sample/model/repository/DemoRepository.kt) |
| [CachedRepository](../core/storage/src/main/kotlin/dev/androidbasekit/storage/CachedRepository.kt) | 缓存优先、网络优先失败回退、仅网络策略 | `get()`（Kotlin suspend） | [MemoryCacheTest（测试）](../core/storage/src/test/kotlin/dev/androidbasekit/storage/MemoryCacheTest.kt) |
| [AppFiles](../core/system/src/main/kotlin/dev/androidbasekit/system/AppFiles.kt) | 创建缓存临时文件、按期限清理 | `createTemp()`、`deleteExpiredTemp()` | 暂无 Sample 直接调用 |

- 文件目录使用 `context.getCacheDir()` / `getFilesDir()` / `getExternalFilesDir()`。普通文本 IO 使用 Java 流或 Kotlin `File.readText()` / `writeText()`，后台执行并显式处理异常；大文件用流。
- `AppFiles` 只清理缓存目录下 `basekit_` 前缀的文件，不提供整个 App 数据删除或任意目录递归清理。
- JSON 由 `NetworkClient` 的 Gson Converter 处理网络 DTO；手动序列化可以在声明了 Gson 依赖的模块中使用 `Gson` / `TypeToken`。当前没有项目级 `JsonConverter` 单例，不预置 Pockly 业务适配器。
- 向其他应用分享文件需由接入 App 配置 FileProvider 或使用已有 content Uri，并设置读权限；`SystemIntents.shareText()` 仅分享文本。
- Room 的 `DemoDatabase`、`DemoDao`、`DemoRecord` 是示例，不把示例表结构当成所有 App 的通用数据模型。

## 8. 网络与链接

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [WebUrls](../core/common/src/main/kotlin/dev/androidbasekit/common/WebUrls.kt) | HTTP(S) 语法检查、UTF-8 查询参数编码及追加 | `isHttpUrl()`、`encodeQueryComponent()`、`appendQueryParameter()` | [PortableToolsTest（测试）](../core/common/src/test/kotlin/dev/androidbasekit/common/PortableToolsTest.kt) |
| [NetworkStatus](../core/system/src/main/kotlin/dev/androidbasekit/system/NetworkStatus.kt) | 默认网络的 INTERNET + VALIDATED 状态与主线程通知 | `isOnline()`、`observe()` | 暂无 Sample 直接调用 |
| [NetworkClient](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkClient.kt) | Retrofit 服务、OkHttp、同源凭证、受控重试和刷新 | `create()`、Java `getOkHttp()` / `getRetrofit()` | [DemoRepository](../sample/src/main/kotlin/dev/androidbasekit/sample/model/repository/DemoRepository.kt) |
| [JavaRequests](../core/network/src/main/kotlin/dev/androidbasekit/network/JavaRequests.kt) | Java 生命周期请求和主线程回调 | `enqueue()` | [JavaInteropTest（测试）](../sample/src/test/java/dev/androidbasekit/sample/JavaInteropTest.java) |
| [BaseRequest](../core/network/src/main/kotlin/dev/androidbasekit/network/BaseRequest.kt) | 请求状态、覆盖旧请求、取消和响应解包 | `forOwner()`、`enqueue()`、`enqueueResponse()`、`cancel()`、`launch()` | [DemoViewModel](../sample/src/main/kotlin/dev/androidbasekit/sample/viewmodel/DemoViewModel.kt)、[JavaInteropTest（测试）](../sample/src/test/java/dev/androidbasekit/sample/JavaInteropTest.java) |
| [ApiErrorMapper](../core/network/src/main/kotlin/dev/androidbasekit/network/ApiErrorMapper.kt) | 异常分类、协程结果包装 | `map()`、`execute()`（Kotlin suspend） | [DemoRepository](../sample/src/main/kotlin/dev/androidbasekit/sample/model/repository/DemoRepository.kt) |
| [ProgressRequestBody / ProgressResponseBody](../core/network/src/main/kotlin/dev/androidbasekit/network/TransferProgress.kt) | 上传/下载流式字节进度 | 构造包装器、标准 RequestBody/ResponseBody 方法 | [NetworkClientTest（测试）](../core/network/src/test/kotlin/dev/androidbasekit/network/NetworkClientTest.kt) |

网络状态为 true 不保证某个服务器可达；URL 格式通过也不意味着目标可信或可访问。`WebUrls` 采用 `java.net.URI`，要求主机可解析，国际化域名应先由调用方转换为 ASCII；不自动去空白、补协议或修复非法 URL。系统浏览器 Intent 校验与请求客户端解析各自独立，不保证接受范围完全相同。

## 9. 生命周期与异步

| 工具类 / 源码 | 用途 | 常用方法 | 调用参考 |
| --- | --- | --- | --- |
| [LifecycleTasks](../core/lifecycle/src/main/kotlin/dev/androidbasekit/lifecycle/LifecycleTasks.kt) | Java 生命周期绑定的后台 Runnable | `launchIo()`，返回 `TaskHandle.cancel()` | 暂无 Sample 直接调用 |
| [AppForegroundObserver](../core/lifecycle/src/main/kotlin/dev/androidbasekit/lifecycle/AppForegroundObserver.kt) | ProcessLifecycleOwner 前后台 LiveData | Java `getForeground()`、`close()` | 暂无 Sample 直接调用 |

Kotlin 使用 `viewModelScope` / `lifecycleScope` 与 `delay()`。Java 后台任务不要直接更新 View，Fragment 请求/观察使用 `getViewLifecycleOwner()`，销毁 View 后不得再读 Binding。`NetworkStatus.observe()` 返回值、`AppForegroundObserver` 都应由持有方结束时关闭。

## 10. 常见 Java 调用示例

### 文本、数字与时长

```java
String title = Texts.orDefault(serverTitle, "默认标题");
String shortTitle = Texts.truncate(title, 20);
String price = Numbers.decimal(new BigDecimal("12.345"), 2); // 12.35
String duration = Durations.formatSeconds(125L);             // 02:05
String elapsed = Durations.formatMillis(3_661_000L);         // 1:01:01
String size = ByteSizes.format(1_572_864L);                  // 1.50 MiB
String digest = Digests.sha256("hello");
```

需导入 `dev.androidbasekit.common.*` 和 `java.math.BigDecimal`。用户文案在正式项目中使用字符串资源。

### 日期与时区

```java
TimeZone utc = TimeZone.getTimeZone("UTC");
String date = Dates.format(0L, "yyyy-MM-dd HH:mm", Locale.US, utc);
Long timestamp = Dates.parse("2024-02-29", "yyyy-MM-dd", Locale.US, utc);
if (timestamp != null) {
    // 毫秒时间戳；解析失败返回 null，非法 pattern 会抛异常。
}
```

需导入 `java.util.Locale` / `TimeZone`。默认重载采用设备地区与时区；服务器固定格式显式传入。

### 布局、着色与点击

```java
int bottomPx = view.getResources().getDimensionPixelSize(bottomPaddingResId);
ViewLayouts.setBottomPadding(view, bottomPx);
// view 必须已拥有父容器分配的 LayoutParams。
ViewLayouts.setSize(view, ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
ViewLayouts.onNextPreDraw(view, () -> {
    int widthPx = view.getWidth();
    // 在当前 View 生命周期内使用 widthPx。
});
int highlightColor = view.getContext().getColor(highlightColorResId);
textView.setText(TextSpans.colorMatches(textView.getText(), keyword, highlightColor));
Clicks.setDebounced(button,
        () -> UiFeedback.snackbar(button, messageStringResId), 800L);
```

需导入 `dev.androidbasekit.ui.*`、`android.view.ViewGroup`。`*ResId` 代表接入 App 的资源 ID。`setMargins()` 使用 start/end，适配 RTL；`setBottomPadding()` 保留其余 padding，不累计旧 bottom。

### MMKV 与缓存

```java
// Application.onCreate() 中一次初始化。
AppPreferences.initialize(getApplication());
// userId 由当前账号提供；open() 无参打开应用设置空间。
AppPreferences prefs = AppPreferences.open(userId);
prefs.putString("theme", "dark");
String theme = prefs.getString("theme", "system");
prefs.remove("theme");

MemoryCache<String> cache = new MemoryCache<>();
cache.put("page", json, 60_000L); // TTL 毫秒，0 表示不过期。
String cached = cache.get("page"); // 未命中或已过期返回 null。
```

需导入 `dev.androidbasekit.storage.*`。示例 key 由接入 App 统一维护。`clearUserData()` 会清空当前实例空间，无参 `open()` 上调用会清空应用设置，退出账号应使用对应用户实例。

### URL、复制与系统分享

```java
String url = WebUrls.appendQueryParameter(
        "https://example.com/search?source=app#results", "q", "a b&c");
// https://example.com/search?source=app&q=a%20b%26c#results

// 用户点击后在主线程执行。
ClipboardTools.copyText(view.getContext(), title, "标题", false);
Intent chooser = Intent.createChooser(SystemIntents.shareText(title), chooserTitle);
boolean opened = SystemIntents.startIfAvailable(activity, chooser);
// opened=false 时由业务页选择提示文案。
```

需导入 `dev.androidbasekit.system.*` 与 `android.content.Intent`。查询参数传原始文本，不提前编码；同名参数追加而非覆盖。`encodeQueryComponent()` 不用于完整 URL、路径或表单正文。

### 图片与请求

```java
ImageLoader.loadAvatar(imageView, avatarUrl);
ImageLoader.loadRounded(imageView, coverUrl, 12); // dp
// RecyclerView 回收或页面清理时取消对应 Target。
ImageLoader.clear(imageView);
```

网络完整调用链可参考 [DemoViewModel](../sample/src/main/kotlin/dev/androidbasekit/sample/viewmodel/DemoViewModel.kt) 和 [DemoRepository](../sample/src/main/kotlin/dev/androidbasekit/sample/model/repository/DemoRepository.kt)：

```text
ViewModel 持有 BaseRequest
  → Repository 发起 Retrofit 请求
  → ApiErrorMapper 转换结果（取消继续传播）
  → BaseRequest 发布状态
  → 页面观察 LiveData，决定保留内容或展示错误
```

Java 请求调用见 [REQUEST_BASE](REQUEST_BASE.md) / [JAVA_INTEROP](JAVA_INTEROP.md)。每次提供新的 Retrofit Call；Fragment 的 `forOwner()` / `enqueue()` 绑定 ViewLifecycleOwner。重试默认关闭，仅对可重放的幂等请求启用。

## 11. Pockly 工具迁移对照与取舍

以下是能力对应，并非类名、参数、行为完全兼容的替换表。

| Pockly 入口 | AndroidBaseKit 对应 / 处理方式 |
| --- | --- |
| `MyTextUtils`、`ListUtil`、`TUtils` | `Texts` + Java/Kotlin 标准库；不引入跨按钮或业务约定 |
| `NumberUtil` | `Numbers` 负责精确数字格式；积分、码率、计数缩写由业务定义 |
| `MySpanUtils` | **新增 `TextSpans`**，支持区间和全部不重叠匹配 |
| `TimeUtil` | `Dates` + **新增 `Durations`**；自然日、业务剩余时间策略由调用方定义 |
| `FileSizeUtil` | **新增 `ByteSizes`**，IEC 单位明确标为 KiB/MiB |
| `ViewUtil` | **新增 `ViewLayouts`**，保留父容器参数类型，明确 px 与 RTL；未包含所有旧方法 |
| `ResourceHelper`、`MySizeUtils` | 当前 Context 的资源 API + `Screen` |
| `ClickUtils`、`ToastUtil`、`SnackbarUtil` | `Clicks`、`UiFeedback`；监听器之间独立防抖 |
| `AnimUtil` | Android 动画 API；由页面负责启动与取消 |
| `GlideUtils` | `ImageLoader`，复杂变换直接使用 Glide |
| `MusicPlayerGradientHelper`、`PlaylistGradientHelper` | 未迁移；产品封面颜色与渐变策略 |
| `EdgeToEdgeHelper`、`SystemBarUtil`、`BarUtil` | 官方 Window/Insets API，接入 Base 页面统一策略；本次未新建包装器 |
| `KeyboardUtils` | `Keyboard` 只提供显隐；未包含高度监听和延时队列 |
| `TextViewCopyUtils`、`SystemUtil.copy` | `ClipboardTools`，不捆绑 Toast |
| `KVUtils` | `AppPreferences` + `MemoryCache` / `PreferencesCacheStore`；当前设置 API 只含字符串和整数 |
| `JsonConverter` | 当前网络 Gson Converter；手动 JSON 在使用模块显式声明依赖，未新增业务 Gson 单例 |
| `FileUtil`、`FilePathUtil`、`FileCopyUtil`、`CleanDataUtils` | `AppFiles` + Context 目录与流 API；下载落盘协议、任意目录清空没有迁移 |
| `HttpHelper`、`RxUtil` | `NetworkClient`、`BaseRequest`、`JavaRequests`、协程、`LifecycleTasks`；不引入 RxJava |
| `HttpUtils`、`ConnectTypeUtil`、`NetSpeedUtils` | `NetworkStatus` 只提供已验证默认网络状态，不估算弱网或带宽 |
| `UrlCheckUtil`、`UrlValidationUtils`、`UrlSchemeUtils` | **新增 `WebUrls`** + `SystemIntents.web()`；不自动补协议，不含 TextView 链接化 |
| `PackageUtil`、`SystemUtil` | `DeviceInfo` 覆盖版本与基础设备信息；不读取唯一设备标识 |
| `DevicesUtil`、`L` | 会话/播放标识由业务维护；日志遵循 App 策略，现有网络日志仅含方法/状态/耗时 |
| `JumpUtil`、`ShareUtils` | `Navigation` / `SystemIntents` 提供通用入口，业务页面映射与媒体分享留在 App |
| `NotificationPermissionHelper`、`PipPermissioUtile` | `Permissions` 只提供通用权限入口；通知可展示性、PiP 能力和引导未实现 |
| `AppLanguageUtils` | 应用语言列表/恢复策略留在 App；资源读取沿用当前 Context |
| `DownloadServerHelper`、`AdInsertListHelper`、`MemberCenterRewardUtils`、`ListBlockInsertListHelper` | 未迁移：下载服务状态、广告、会员奖励和内容屏蔽依赖特定项目 |

## 12. 使用细节与易混淆点

1. **文本长度**：`Texts.truncate()` 按码点，suffix 额外添加；`TextSpans.colorRange()` 按 UTF-16 索引，不能混用。码点截断不保证组合 emoji 整体保留。
2. **日期**：`Dates.parse()` 拒绝无效日历日期和尾随内容，不保证固定宽度输入掩码；每次使用独立 formatter。非法 pattern 抛异常。
3. **时长**：`formatSeconds(125)` 与 `formatMillis(125_000)` 都是 `02:05`。毫秒不足一秒部分丢弃；负值明确抛异常，未知时长由业务展示占位文案。
4. **文件大小**：`ByteSizes.format()` 接受非负 Long；B 显示整数，其他单位默认两位 HALF_UP。单位按舍入前选择，所以 `1_048_575` 显示 `1024.00 KiB`；不把 1024 进制标成十进制 MB。
5. **点击**：默认间隔 600ms，使用单调时钟。每个监听器独立，重新设置监听器重置窗口；主线程使用。
6. **布局**：尺寸为 px，宽高接受 MATCH_PARENT/WRAP_CONTENT；`setSize()` 要求已有 LayoutParams，`setMargins()` 要求 MarginLayoutParams，缺少时抛异常，不猜测父容器类型。
7. **预绘制**：`onNextPreDraw()` 执行一次，脱离窗口自动移除；返回监听器可主动取消。它不保证销毁后的旧 View 会重新收到回调。
8. **关键词**：`TextSpans.colorMatches()` 区分大小写、不重叠；空关键词返回副本。保留原 span 且不修改原文本，再次传入已着色结果可能保留之前颜色，重新绑定时应使用权威原文。
9. **存储**：MMKV 先初始化，用户切换打开新空间。TTL 单位毫秒，0 为不过期；内存缓存默认单调时钟，持久缓存使用墙上时钟，会受系统时间变化影响。
10. **URL**：`WebUrls` 不执行网络访问；只负责语法与查询编码，不是 URL 信任策略。`appendQueryParameter()` 保留旧参数/fragment，原有转义不重复编码。
11. **剪贴板**：sensitive 仅设置系统敏感预览标记，不加密内容；不后台读取或监听剪贴板。Android 13+ 具体预览行为需设备验证。
12. **IO 与线程**：文件摘要、文件遍历和磁盘缓存清理放后台；`Digests.sha256(stream)` 不关闭调用方流，文件重载自行关闭，IO 异常继续抛出。摘要不等于加密。
13. **请求与生命周期**：取消不转普通错误；进度回调在传输线程，UI 自行切主线程与节流。销毁 View 后不能再操作 Binding；响应体仍由持有方关闭。

## 13. 范围与验证

- 本文是工具选用与迁移索引，所有类的完整职责见 [CLASS_GUIDE](CLASS_GUIDE.md)，API 与接入方式见 [API](API.md) / [README](../README.md)。
- 本次新增五个工具，无新增第三方依赖，无调整 SDK/AGP/JDK，无自动开启权限申请、清理任务或外部页面。
- 新增测试覆盖时长单位/小时边界/Long 上限、IEC 换算/舍入/非法参数、URL 编码/fragment/重复参数、Java 重载、原文本样式保留、父容器参数保留、RTL padding、预绘制一次性执行与取消。
- 实际执行结果与未验证边界见 [VALIDATION](VALIDATION.md) 中“2026-10-17 通用工具补齐”。JVM/Robolectric 不替代设备上的布局、RTL、键盘、系统分享与系统栏验证。
