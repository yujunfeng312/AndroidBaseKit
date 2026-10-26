# 类职责索引

日期：2026-10-17。所有命名类、接口、枚举、object 均在源码声明处补有 KDoc/Javadoc；本表按模块列出生产源码，包含内部辅助类型。测试类说明放在对应测试源码。

每项包含该类型的主要功能及使用边界；完整方法签名见源码和 [API](API.md)。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 基础类型与工具 | [core/common](#corecommon) | 结果、错误、常量、文本、日期、数字、URL 与摘要 |
| 图片 | [core/image](#coreimage) | 加载入口、选项与回调 |
| 生命周期 | [core/lifecycle](#corelifecycle) | 后台任务、取消句柄与前后台观察 |
| 网络 | [core/network](#corenetwork) | 客户端、请求状态、认证、重试与传输进度 |
| 存储 | [core/storage](#corestorage) | 设置、缓存、策略及 Room 示例 |
| 系统 | [core/system](#coresystem) | 屏幕、键盘、权限、网络状态、剪贴板和 Intent |
| 界面 | [core/ui](#coreui) | Binding 基类、状态、控件、提示和布局 |
| 示例业务 | [sample](#sample) | Java 页面、ViewModel、Repository、DTO 与 Worker |

## core/common

| 类型 | 主要功能与边界 |
|---|---|
| [`ApiCodes`](../core/common/src/main/kotlin/dev/androidbasekit/common/ApiCodes.kt) | HTTP 通用状态码常量。集中定义未授权、限流等协议码；业务成功码由 BaseResponse 的调用方配置。 |
| [`ApiResult`](../core/common/src/main/kotlin/dev/androidbasekit/common/ApiResult.kt) | 统一的成功/失败结果。提供 Success、Failure 及 Java 静态工厂；协程取消应继续抛出，不包装成失败。 |
| [`Success`](../core/common/src/main/kotlin/dev/androidbasekit/common/ApiResult.kt) | 成功结果，保存业务数据 data。调用方可通过 ApiResult.getOrNull 读取。 |
| [`Failure`](../core/common/src/main/kotlin/dev/androidbasekit/common/ApiResult.kt) | 失败结果，保存结构化 AppError。由 UI 根据错误类型选择本地化提示。 |
| [`AppConfig`](../core/common/src/main/kotlin/dev/androidbasekit/common/AppConfig.kt) | 框架默认配置常量。提供最低 API 和默认网络超时；Android 构建 SDK 以 Gradle 配置为准。 |
| [`ErrorKind`](../core/common/src/main/kotlin/dev/androidbasekit/common/AppError.kt) | 错误分类枚举。区分 HTTP、网络、超时、解析、业务、缓存和未知错误，供上层统一展示。 |
| [`AppError`](../core/common/src/main/kotlin/dev/androidbasekit/common/AppError.kt) | 结构化错误信息。kind 表示分类，technicalCode 仅用于诊断；不要将异常响应正文直接展示给用户。 |
| [`BusinessException`](../core/common/src/main/kotlin/dev/androidbasekit/common/AppError.kt) | 业务失败信号，保存服务端业务码。交给 ApiErrorMapper 映射为 BUSINESS，不保存完整响应。 |
| [`BundleKeys`](../core/common/src/main/kotlin/dev/androidbasekit/common/BundleKeys.kt) | Bundle 参数键。集中管理页面参数名称，供 Fragment arguments 和状态传递使用。 |
| [`CacheKeys`](../core/common/src/main/kotlin/dev/androidbasekit/common/CacheKeys.kt) | 缓存条目的键名。用于 Repository 读写缓存；用户隔离由具体 CacheStore 实例负责。 |
| [`Dates`](../core/common/src/main/kotlin/dev/androidbasekit/common/Dates.kt) | 毫秒时间戳格式化、指定地区/时区、严格完整解析。 |
| [`Durations`](../core/common/src/main/kotlin/dev/androidbasekit/common/Durations.kt) | 非负秒/毫秒时长显示为 mm:ss 或 H:mm:ss，小时不按天回绕。 |
| [`ByteSizes`](../core/common/src/main/kotlin/dev/androidbasekit/common/ByteSizes.kt) | 非负字节数按 1024 换算为 B/KiB/MiB 等 IEC 单位，支持指定小数位。 |
| [`Digests`](../core/common/src/main/kotlin/dev/androidbasekit/common/Digests.kt) | 文本、字节、流和文件 SHA-256。 |
| [`IntentKeys`](../core/common/src/main/kotlin/dev/androidbasekit/common/IntentKeys.kt) | Activity Intent 返回值的键名。由发起页与结果页共同使用，避免散落字符串。 |
| [`Numbers`](../core/common/src/main/kotlin/dev/androidbasekit/common/Numbers.kt) | BigDecimal 舍入、固定小数位、本地化分组。 |
| [`PreferenceKeys`](../core/common/src/main/kotlin/dev/androidbasekit/common/PreferenceKeys.kt) | 持久化设置键名。配合 AppPreferences 保存设置，不承担存储初始化或用户隔离。 |
| [`RequestTags`](../core/common/src/main/kotlin/dev/androidbasekit/common/RequestTags.kt) | 业务请求标识常量。用于调用方区分请求场景；声明标识本身不会取消或启动请求。 |
| [`Texts`](../core/common/src/main/kotlin/dev/androidbasekit/common/Texts.kt) | 空白判断、默认值、按码点截断。 |
| [`WebUrls`](../core/common/src/main/kotlin/dev/androidbasekit/common/WebUrls.kt) | HTTP(S) 语法检查、UTF-8 查询参数编码/追加，保留现有查询与 fragment，不访问网络。 |
| [`TimeConstants`](../core/common/src/main/kotlin/dev/androidbasekit/common/TimeConstants.kt) | 时间单位常量，统一使用毫秒。用于缓存 TTL 和超时计算，避免秒与毫秒混用。 |

## core/image

| 类型 | 主要功能与边界 |
|---|---|
| [`ImageCallback`](../core/image/src/main/kotlin/dev/androidbasekit/image/ImageLoader.kt) | 图片加载结果通知。只提供最终成功/失败，复杂 Target 和数据源处理可直接使用 Glide。 |
| [`ImageScale`](../core/image/src/main/kotlin/dev/androidbasekit/image/ImageLoader.kt) | 图片缩放策略。CENTER_CROP 裁剪填充，FIT_CENTER 完整居中展示。 |
| [`ImageOptions`](../core/image/src/main/kotlin/dev/androidbasekit/image/ImageLoader.kt) | 图片请求选项。配置占位/错误图、缩放、内存/磁盘缓存、缩略图、GIF、圆形及圆角；圆角单位 dp。 |
| [`ImageLoader`](../core/image/src/main/kotlin/dev/androidbasekit/image/ImageLoader.kt) | Glide 常用入口。提供普通图、头像、圆角、GIF、预加载及缓存清理；加载绑定 View，磁盘清理在后台执行。 |

## core/lifecycle

| 类型 | 主要功能与边界 |
|---|---|
| [`AppForegroundObserver`](../core/lifecycle/src/main/kotlin/dev/androidbasekit/lifecycle/AppForegroundObserver.kt) | 应用进程前后台观察器。通过 LiveData 提供 ProcessLifecycleOwner 状态，无页面引用；由持有方 close 解除观察。 |
| [`TaskHandle`](../core/lifecycle/src/main/kotlin/dev/androidbasekit/lifecycle/LifecycleTasks.kt) | 生命周期后台任务的取消句柄。取消是协作式的，Runnable 应响应中断。 |
| [`LifecycleTasks`](../core/lifecycle/src/main/kotlin/dev/androidbasekit/lifecycle/LifecycleTasks.kt) | Java 生命周期后台任务入口。在 IO 调度器运行 Runnable，LifecycleOwner 销毁时取消；Kotlin 挂起任务可直接用 lifecycleScope。 |

## core/network

| 类型 | 主要功能与边界 |
|---|---|
| [`ApiErrorMapper`](../core/network/src/main/kotlin/dev/androidbasekit/network/ApiErrorMapper.kt) | 异常与请求结果的转换入口。区分网络、超时、HTTP、JSON 解析及业务异常；保留协程取消语义。 |
| [`BaseRequest`](../core/network/src/main/kotlin/dev/androidbasekit/network/BaseRequest.kt) | 有生命周期作用域的请求控制器。提供 Kotlin launch/launchResult、Java enqueue/enqueueResponse、取消和 LiveData 状态；新请求覆盖旧请求，UI 状态更新在主线程调用。 |
| [`BaseResponse`](../core/network/src/main/kotlin/dev/androidbasekit/network/BaseResponse.kt) | 可选的 code/message/data 服务端响应模型。支持自定义成功码和非空数据校验；无统一包裹结构的接口直接使用 ApiResult。 |
| [`CancelHandle`](../core/network/src/main/kotlin/dev/androidbasekit/network/JavaRequests.kt) | Java 请求取消句柄。取消对应任务及其 Retrofit Call；取消不回调为普通业务失败。 |
| [`ResultCallback`](../core/network/src/main/kotlin/dev/androidbasekit/network/JavaRequests.kt) | Java 请求结果接口。成功返回数据，失败返回 AppError；由 JavaRequests 在主线程交付。 |
| [`JavaRequests`](../core/network/src/main/kotlin/dev/androidbasekit/network/JavaRequests.kt) | Java 生命周期请求桥接。绑定 LifecycleOwner、主线程交付回调、销毁时取消；每次传入新的 Retrofit Call。 |
| [`EmptyBodyException`](../core/network/src/main/kotlin/dev/androidbasekit/network/JavaRequests.kt) | HTTP 成功但缺少预期响应体的异常。用于错误映射，避免将空响应误当作非空业务数据。 |
| [`NetworkClient`](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkClient.kt) | Retrofit/OkHttp 客户端入口。创建 API 服务、注入同源凭证、受控重试及单次 Token 刷新；日志仅记录方法、状态码、耗时。 |
| [`RetryInterceptor`](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkClient.kt) | 内部重试拦截器。仅重放幂等且可重复发送的请求，限制次数与退避；响应关闭后才重试，并检查取消。 |
| [`RefreshAuthenticator`](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkClient.kt) | 内部 401 认证处理器。同一客户端串行协调 Token 刷新，限制重放并对失败 Token 短暂冷却；不承担登录 UI。 |
| [`TokenProvider`](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkConfig.kt) | 当前 Token 的读取接口。每次需要认证时读取最新值；实现方负责线程安全，不持有页面对象。 |
| [`TokenRefresher`](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkConfig.kt) | Token 刷新接口。在 OkHttp 工作线程执行；使用独立客户端并原子保存新 Token，避免递归刷新和并发读到旧值。 |
| [`RetryPolicy`](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkConfig.kt) | 有限重试策略。配置重试次数与退避上限，默认不重试；只对可重放的幂等请求生效。 |
| [`NetworkConfig`](../core/network/src/main/kotlin/dev/androidbasekit/network/NetworkConfig.kt) | 网络客户端配置。包含 baseUrl、Header 快照、超时、Token 提供/刷新及重试策略；baseUrl 必须以斜线结束。 |
| [`RequestStatus`](../core/network/src/main/kotlin/dev/androidbasekit/network/RequestState.kt) | 请求执行状态。包含空闲、执行中、成功、失败、取消；与页面是否保留旧内容分开处理。 |
| [`RequestState`](../core/network/src/main/kotlin/dev/androidbasekit/network/RequestState.kt) | 单次请求状态快照。提供 status、result 以及便捷 data/error 属性，通过 BaseRequest.state 观察。 |
| [`ProgressListener`](../core/network/src/main/kotlin/dev/androidbasekit/network/TransferProgress.kt) | 上传或下载字节进度回调。在传输线程触发，totalBytes 为 -1 表示未知；UI 更新需自行切主线程和节流。 |
| [`ProgressRequestBody`](../core/network/src/main/kotlin/dev/androidbasekit/network/TransferProgress.kt) | 上传请求体装饰器。统计写入字节并报告完成，透传内容类型、长度和 one-shot/duplex 语义。 |
| [`ProgressResponseBody`](../core/network/src/main/kotlin/dev/androidbasekit/network/TransferProgress.kt) | 下载响应体装饰器。统计实际读取的字节，读到 EOF 才报告完成；调用方仍须关闭响应体。 |

## core/storage

| 类型 | 主要功能与边界 |
|---|---|
| [`AppPreferences`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/AppPreferences.kt) | MMKV 设置存储。先在 Application 初始化，再按应用/用户打开；提供字符串、整数和清理，不是加密凭证保险箱。 |
| [`CacheStore`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/CacheStore.kt) | 有作用域的缓存存取协议。包含读、写、删除、清空和过期清理；ttlMillis 单位毫秒，当前实现中 0 表示永久。 |
| [`CacheClock`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/CacheStore.kt) | 可注入的毫秒时钟。供 TTL 计算和测试使用；同一缓存的读写必须使用同一时间基准。 |
| [`CachePolicy`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/CacheStore.kt) | Repository 缓存策略。提供缓存优先、网络优先失败回退、仅网络三种模式。 |
| [`CacheException`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/CacheStore.kt) | 缓存访问失败的包装异常。区分缓存层故障与网络失败，由调用方决定记录或降级。 |
| [`CachedRepository`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/CachedRepository.kt) | 组合缓存与远程数据的 Repository 辅助类。按 CachePolicy 读写，缓存失败单独报告，不覆盖成功的网络数据。 |
| [`DemoRecord`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/DemoDatabase.kt) | Room 示例数据实体。userId 与 id 组成主键，保存标题、图片地址和更新时间；正式业务可替换表结构。 |
| [`DemoDao`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/DemoDatabase.kt) | Room 示例 DAO。按用户观察/查询/删除数据，支持事务替换和过期清理；挂起查询不阻塞主线程。 |
| [`DemoDatabase`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/DemoDatabase.kt) | Room 示例数据库。提供 DAO、导出 schema 和显式 1→2 迁移；持有方在任务结束后关闭数据库。 |
| [`MemoryCache`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/MemoryCache.kt) | 线程安全的内存 LRU/TTL 缓存。限制容量、访问时淘汰过期项，默认使用单调时钟；0 TTL 不过期。 |
| [`Entry`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/MemoryCache.kt) | 内部缓存记录。将值与过期时间保存在一起，仅供所在缓存实现使用。 |
| [`PreferencesCacheStore`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/PreferencesCacheStore.kt) | MMKV 字符串持久缓存。与设置分命名空间，按用户隔离并维护 TTL；使用墙上时钟，系统时间变化会影响期限。 |
| [`Entry`](../core/storage/src/main/kotlin/dev/androidbasekit/storage/PreferencesCacheStore.kt) | 内部缓存记录。将值与过期时间保存在一起，仅供所在缓存实现使用。 |

## core/system

| 类型 | 主要功能与边界 |
|---|---|
| [`AppFiles`](../core/system/src/main/kotlin/dev/androidbasekit/system/AppFiles.kt) | 应用缓存目录临时文件工具。创建 basekit_ 前缀文件、清理过期文件；文件 IO 在后台执行，不遍历业务目录。 |
| [`ClipboardTools`](../core/system/src/main/kotlin/dev/androidbasekit/system/ClipboardTools.kt) | 主动复制文本、敏感预览标记。 |
| [`AppVersion`](../core/system/src/main/kotlin/dev/androidbasekit/system/DeviceInfo.kt) | 应用版本快照。包含可展示的版本名 name 和长整型版本号 code。 |
| [`DeviceInfo`](../core/system/src/main/kotlin/dev/androidbasekit/system/DeviceInfo.kt) | 应用与设备基础信息入口。读取版本、API、型号和厂商，不读取设备唯一标识。 |
| [`Keyboard`](../core/system/src/main/kotlin/dev/androidbasekit/system/Keyboard.kt) | 软键盘显示/隐藏入口。要求 View 使用 Activity Context；显示前请求焦点，使用窗口 Insets 控制 IME。 |
| [`Navigation`](../core/system/src/main/kotlin/dev/androidbasekit/system/Navigation.kt) | Activity 导航与结果注册工具。创建显式 Intent、注册 Activity Result；在 STARTED 之前完成注册。 |
| [`NetworkListener`](../core/system/src/main/kotlin/dev/androidbasekit/system/NetworkStatus.kt) | 网络可用性回调。接收具备 INTERNET 且 VALIDATED 的联网状态，由 NetworkStatus 主线程交付。 |
| [`NetworkStatus`](../core/system/src/main/kotlin/dev/androidbasekit/system/NetworkStatus.kt) | 网络可用性查询与观察。依据默认网络 INTERNET/VALIDATED 能力判断；observe 返回订阅，页面结束时 close。 |
| [`PermissionManager`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/PermissionManager.kt) | 可继承的权限申请流程，支持 Activity/Fragment、生命周期和并发保护。 |
| [`PermissionOptions`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/PermissionOptions.kt) | 单次请求的成功/拒绝回调、解释与永久拒绝弹框配置及自定义处理。 |
| [`PermissionFlow`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/PermissionFlow.kt) | 原生权限状态机，处理解释、重试、永久拒绝与设置返回复查。 |
| [`PermissionPrompt`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/PermissionPrompt.kt) | 一次性用户决策与原生/自定义弹框的生命周期。 |
| [`PermissionRequestFragment`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/PermissionRequestFragment.kt) | 原生 Activity Result launcher 的无界面宿主。 |
| [`NativePermissionSession`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/NativePermissionSession.kt) | 单次请求与宿主生命周期、弹框和回调的绑定清理。 |
| [`CameraPermissionManager`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/CameraPermissionManager.kt) | 相机权限与专用解释文案。 |
| [`MicrophonePermissionManager`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/MicrophonePermissionManager.kt) | 麦克风权限，支持业务继续继承。 |
| [`NotificationPermissionManager`](../core/system/src/main/kotlin/dev/androidbasekit/system/permission/NotificationPermissionManager.kt) | Android 13+ 通知运行时权限。 |
| [`Permissions`](../core/system/src/main/kotlin/dev/androidbasekit/system/Permissions.kt) | 运行时权限底层查询与注册兼容入口；完整流程使用 PermissionManager。 |
| [`Screen`](../core/system/src/main/kotlin/dev/androidbasekit/system/Screen.kt) | 屏幕尺寸与单位换算工具。提供 dp/sp 转 px 和已布局窗口边界；多窗口下不假定设备屏幕就是可用窗口。 |
| [`SystemIntents`](../core/system/src/main/kotlin/dev/androidbasekit/system/SystemIntents.kt) | 网页/分享/应用设置 Intent 与受控启动。 |

## core/ui

| 类型 | 主要功能与边界 |
|---|---|
| [`BindingInflater`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/BindingInflater.kt) | ViewBinding 三参静态工厂，支持 Java/Kotlin 方法引用；Base 统一 attachToParent=false。 |
| [`BaseActivity`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/BaseActivity.kt) | 接收 Binding 工厂并统一 inflate、setContentView 和初始化；兼容原 inflateBinding 重写。 |
| [`BaseDialogFragment`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/BaseDialogFragment.kt) | DialogFragment 内容视图基类。子类实现 createContentView，并自行在视图生命周期结束时释放 Binding。 |
| [`BaseFragment`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/BaseFragment.kt) | 接收 Binding 工厂，统一使用当前 container 创建视图；销毁视图清空 Binding，支持无参重建及旧重写入口。 |
| [`BaseViewModel`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/BaseViewModel.kt) | 轻量页面 ViewModel 基类。提供 pageState 和 requestBusy 两组 LiveData，不持有 Activity、Fragment 或 View。 |
| [`ListLoadStateAdapter`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/ListLoadStateAdapter.kt) | 列表状态 Adapter。配合 ConcatAdapter 展示加载、空数据或错误重试行，不清空业务 ListAdapter 的数据。 |
| [`Holder`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/ListLoadStateAdapter.kt) | 列表行 ViewHolder。保存本行视图或 Binding，配合所属 Adapter 完成复用与绑定。 |
| [`LoadingDialog`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/LoadingDialog.kt) | 简单加载对话框。基于 BaseDialogFragment 显示进度视图，由页面按自身生命周期展示和关闭。 |
| [`LoadingTextView`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/LoadingTextView.kt) | 可点击加载文本控件。支持点击自动 Loading、防重复点击、文字/图标恢复及可配置指示器；隐藏或移除时暂停动画，不执行网络请求。 |
| [`PageStatus`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/PageState.kt) | 页面展示状态。加载、内容、空数据、错误；刷新已有内容时可保留 CONTENT 并单独显示请求忙碌。 |
| [`PageState`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/PageState.kt) | 不可变的页面状态模型。保存展示状态及可选字符串资源 ID，提供 Java/Kotlin 工厂方法。 |
| [`PageStateView`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/PageStateView.kt) | 页面状态视图。显式绑定内容 View 和重试动作，根据 PageState 切换进度、提示、重试和内容可见性。 |
| [`UiFeedback`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/UiFeedback.kt) | 简短 UI 反馈入口。通过字符串资源显示 Toast/Snackbar，调用方在主线程使用当前页面 View。 |
| [`Clicks`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/UiFeedback.kt) | View 点击防抖工具。每个监听器独立使用单调时钟控制点击间隔；重新绑定监听器会重置防抖状态。 |
| [`TextSpans`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/TextSpans.kt) | 返回保留已有样式的文本副本，对 UTF-16 区间或不重叠关键词匹配着色，不修改原文本。 |
| [`ViewLayouts`](../core/ui/src/main/kotlin/dev/androidbasekit/ui/ViewLayouts.kt) | px 尺寸、RTL 相对 margin、底部 padding、可取消的一次性预绘制；保留父容器 LayoutParams 类型。 |

## sample

| 类型 | 主要功能与边界 |
|---|---|
| [`DemoActivity`](../sample/src/main/java/dev/androidbasekit/sample/view/activity/DemoActivity.java) | Sample Java 入口页。继承 Kotlin BaseActivity，通过 ViewBinding 承载导航页面。 |
| [`DemoAdapter`](../sample/src/main/java/dev/androidbasekit/sample/view/adapter/DemoAdapter.java) | Java ListAdapter 示例。通过 DiffUtil 更新 DemoRecord 列表，绑定图片及点击，回收时清理图片请求。 |
| [`ItemClick`](../sample/src/main/java/dev/androidbasekit/sample/view/adapter/DemoAdapter.java) | 示例列表点击接口。将被点击的数据交回页面，不在 Adapter 中直接导航。 |
| [`Holder`](../sample/src/main/java/dev/androidbasekit/sample/view/adapter/DemoAdapter.java) | 列表行 ViewHolder。保存本行视图或 Binding，配合所属 Adapter 完成复用与绑定。 |
| [`DemoApplication`](../sample/src/main/java/dev/androidbasekit/sample/DemoApplication.java) | Sample Application。初始化 MMKV 与调试日志，是框架初始化接入示例。 |
| [`DemoFragment`](../sample/src/main/java/dev/androidbasekit/sample/view/fragment/DemoFragment.java) | Sample Java 功能页。观察 ViewModel，演示列表、Loading/Empty/Error、刷新和 Activity Result；观察绑定视图生命周期。 |
| [`DetailsFragment`](../sample/src/main/java/dev/androidbasekit/sample/view/fragment/DetailsFragment.java) | Sample Java 详情页。读取 Navigation 参数并显示内容，演示 BaseFragment/ViewBinding 的轻量接入。 |
| [`ResultActivity`](../sample/src/main/java/dev/androidbasekit/sample/view/activity/ResultActivity.java) | Sample 结果返回页。通过 IntentKeys 返回文本，演示 Activity Result 协议。 |
| [`CacheCleanupWorker`](../sample/src/main/kotlin/dev/androidbasekit/sample/worker/CacheCleanupWorker.kt) | WorkManager 后台清理示例。周期删除旧记录，失败有限重试；通过 schedule 显式启用，不自动启动。 |
| [`Photo`](../sample/src/main/kotlin/dev/androidbasekit/sample/model/entity/Photo.kt) | JSONPlaceholder 图片响应 DTO。保存 id、标题和缩略图地址，映射为本地 DemoRecord 后展示。 |
| [`DemoApi`](../sample/src/main/kotlin/dev/androidbasekit/sample/model/api/DemoApi.kt) | Retrofit 示例接口。演示 suspend photos 和 Java Call photosCall 两种调用形式。 |
| [`DemoRepository`](../sample/src/main/kotlin/dev/androidbasekit/sample/model/repository/DemoRepository.kt) | 示例数据仓库。组合 Room、MMKV、内存缓存和远程 API，提供本地初始化、网络刷新与用户清理；持有数据库并负责关闭。 |
| [`DemoViewModel`](../sample/src/main/kotlin/dev/androidbasekit/sample/viewmodel/DemoViewModel.kt) | 示例页面状态持有者。协调 BaseRequest、Repository、列表和错误状态；销毁后等待数据库任务完成再关闭仓库。 |
| [`Factory`](../sample/src/main/kotlin/dev/androidbasekit/sample/viewmodel/DemoViewModel.kt) | DemoViewModel 创建工厂。向 ViewModel 注入 Application，不把 Activity Context 保存在 ViewModel 中。 |
