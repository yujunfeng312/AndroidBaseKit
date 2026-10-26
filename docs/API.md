# 公共 API

包名统一为 `dev.androidbasekit.<module>`；Sample API 不属于框架稳定接口。下表只列主要入口，精确签名以 Kotlin 源码为准。可空参数和返回值由 Kotlin 编译器生成 Java Nullable/NotNull 约束。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 基础类型与工具 | [Common](#common) | 结果、错误、常量和通用文本/数字/时间工具 |
| 网络配置与调用 | [Network](#network) | 客户端、认证、重试、Java 回调与传输进度 |
| 请求状态与解包 | [请求 Base](#请求-base) | BaseRequest、BaseResponse 与 RequestState |
| 图片 | [Image](#image) | Glide 入口、选项、取消和清缓存 |
| 存储 | [Storage](#storage) | MMKV、LRU/TTL、Repository 与 Room |
| 生命周期 | [Lifecycle](#lifecycle) | 前后台、后台任务与取消 |
| 界面 | [UI](#ui) | Binding 基类、页面状态、控件和 View 工具 |
| 主题资源 | [常用颜色与主题](#常用颜色与主题) | 语义颜色、深色模式与主题 |
| 系统能力 | [System](#system) | 导航、权限、网络状态、键盘、文件和设备信息 |
| 工具与源码导航 | [常用工具与类职责](#常用工具与类职责) | 工具使用文档和生产类型职责索引 |
| 业务接入边界 | [业务适配点](#业务适配点) | 由接入 App 提供的协议、数据和页面策略 |

## Common

| 类型 | API / 语义 |
|---|---|
| ApiResult<T> | 静态 success(value)、failure(error)；getOrNull()、errorOrNull()、isSuccess()；Kotlin Success/Failure |
| AppError / ErrorKind | HTTP、NETWORK、TIMEOUT、PARSE、BUSINESS、CACHE、UNKNOWN；technicalCode 为诊断码 |
| BusinessException | Repository 根据业务响应 code 显式抛出；不会猜测各 App 的响应格式 |
| ApiCodes、IntentKeys、BundleKeys、PreferenceKeys、CacheKeys、RequestTags、TimeConstants、AppConfig | 分类静态 const 字段，无全局整数型 Activity 请求码 |
| Texts | isBlank(CharSequence?)、orDefault(String?,fallback)、truncate(value,maxCodePoints,suffix="…")；按码点截断，suffix 额外添加 |
| Dates | format(epochMillis,pattern,locale?,timeZone?)、parse(text,pattern,locale?,timeZone?): Long?；省略地区/时区时使用设备默认值，时间戳为毫秒 |
| Numbers | decimal(BigDecimal,scale,roundingMode=HALF_UP)、grouped(BigDecimal,scale=2,locale=设备默认)；固定精度与地区分组 |
| Digests | sha256(String / ByteArray / InputStream / File)；UTF-8、小写十六进制，调用方流不关闭，文件 IO 放后台 |
| Durations | formatSeconds(long)、formatMillis(long)；非负时长显示为 mm:ss 或 H:mm:ss，毫秒不足一秒丢弃 |
| ByteSizes | format(bytes,fractionDigits=2)；1024 进制 B/KiB/MiB 等，非负字节、小数位 0..6 |
| WebUrls | isHttpUrl(String?)、encodeQueryComponent(String)、appendQueryParameter(url,name,value)；保留现有查询和 fragment，不访问网络 |

## Network

| 类型 | API / 约束 |
|---|---|
| NetworkConfig | Java 可 new NetworkConfig(url)；URL 必须以 / 结尾；可配置 provider、refresher、headers、timeoutSeconds、debugLogging、RetryPolicy |
| NetworkClient | create(Class<T>)、getOkHttp()、getRetrofit()；通常每个服务/账号作用域复用一个 client |
| TokenProvider | getToken(): String?；线程安全读取 |
| TokenRefresher | refreshToken(): String?；OkHttp 工作线程调用，用独立 client 刷新并持久化 Token |
| RetryPolicy | maxRetries=0、backoffMillis=200、maxBackoffMillis=2000；幂等可重放请求，有限重试 |
| ApiErrorMapper | Java map(Exception)、Kotlin execute { suspend API }；取消抛出而非包装 |
| JavaRequests | enqueue(owner, Call<T>, ResultCallback<T>): CancelHandle；Main 线程注册，Main 回调；await(call) 是 Kotlin suspend 入口 |
| ResultCallback<T> / CancelHandle | onSuccess(T)、onError(AppError)；cancel()，取消后不发业务错误 |
| ProgressRequestBody / ProgressResponseBody | 包装已有 Body；ProgressListener.onProgress(bytes,total,done) 工作线程回调；读取响应后关闭 |

```java
DemoApi api = new NetworkClient(new NetworkConfig("https://example.com/")).create(DemoApi.class);
CancelHandle handle = JavaRequests.enqueue(getViewLifecycleOwner(), api.photosCall(12),
    new ResultCallback<List<Photo>>() {
        public void onSuccess(List<Photo> value) { /* update ViewModel / UI */ }
        public void onError(AppError error) { /* map kind to strings.xml */ }
    });
// handle.cancel();
```

每次 enqueue 使用新的 Call，不能重复执行同一个 Retrofit Call。此回调 API 不需要 Java 手工使用 suspend。

## 请求 Base

- BaseResponse<T>：code/message/data；checkSuccess(successCode=0)、requireData(successCode=0)，支持 Java 重载。按实际接口协议配置成功码。
- BaseRequest<T>：Kotlin 构造 BaseRequest(viewModelScope)；Java 静态 forOwner(owner)；enqueue(Call<T>)、enqueueResponse(Call<BaseResponse<T>>,successCode=0)、cancel()、getState(): LiveData<RequestState<T>>。
- RequestState<T>：getStatus()/getData()/getError()/getResult()；RequestStatus 为 IDLE/RUNNING/SUCCESS/ERROR/CANCELLED，独立于 PageState。
- Kotlin launch/launchResult 管理 suspend 请求；同实例新请求取消旧任务；取消传播，不映射业务异常。

示例和并发/生命周期约束见 [REQUEST_BASE.md](REQUEST_BASE.md)。

## Image

- `ImageLoader.load(ImageView, Object?)`，model 可 URL、File、Uri、drawable resource ID。
- `load(view, model, ImageOptions, ImageCallback?)`；options 支持 placeholder/error 资源、CENTER_CROP/FIT_CENTER、diskCache、memoryCache、thumbnail、gif、circle、radiusDp。
- `loadAvatar(view, model)`、`loadRounded(view, model, radiusDp)`、`clear(view)`、`preload(context,model)`、`clearMemory(context)` 在 Main 使用。
- Kotlin `clearDisk(context)` 在 IO 执行；Java `clearDiskAsync(context, executor)` 返回 CompletableFuture，executor 必须为后台 executor。
- `ImageCallback.onSuccess()/onFailure()` 表示最终请求结果，不替代 Glide 的全部 Target/RequestListener 能力。复杂 Signature、组合缩略图、尺寸 override 可以直接用 Glide。

## Storage

| 类型 | API / 约束 |
|---|---|
| AppPreferences | initialize(Application Context)、open(userId?)；put/getString、put/getInt、remove、clearUserData。null user 是 App scope，仅清除当前打开 scope |
| CacheStore<V> | get(key): V?、put(key,value,ttlMillis)、remove、clear、deleteExpired(): Int；0 TTL 不过期 |
| MemoryCache<V> | 构造 capacity=100，可注入 CacheClock；线程安全、条目容量 LRU；账号独立实例 |
| PreferencesCacheStore | 构造 userId?、可注入 wall-clock；字符串持久化，独立 cache namespace；异常抛 CacheException |
| CachedRepository<V> | Kotlin suspend get(key,policy,ttl,fetch)；cache error callback；Java 通过业务 ViewModel 暴露结果 |
| CachePolicy | CACHE_FIRST、NETWORK_FIRST、NETWORK_ONLY，已实现对应策略 |
| DemoDatabase | open(context)、items()、MIGRATION_1_2；实例由业务拥有并关闭 |
| DemoDao / DemoRecord | observe(user): LiveData、suspend read/insert/replaceUser/clearUser/deleteOlderThan；用户隔离、事务替换 |

MMKV 中的 getters 使用明确默认值；不把读不到的数据与“写入成功”混为一谈。Room 写入和缓存大数据序列化应放入 IO，禁止 allowMainThreadQueries。

## Lifecycle

- `AppForegroundObserver.getForeground(): LiveData<Boolean>`；Application scope 中创建，close() 移除观察者，Main 线程操作。
- `LifecycleTasks.launchIo(owner,Runnable): TaskHandle`；销毁时取消可中断任务，TaskHandle.cancel() 可提前结束。
- Fragment 直接使用官方 ViewLifecycleOwner；Kotlin 使用官方 lifecycleScope/repeatOnLifecycle，Base 不重新封装其复杂语义。
- WorkManager 经 lifecycle 模块引入，Sample 的 CacheCleanupWorker 是业务示例，schedule(context) 注册唯一周期任务；默认不自动调度。

## UI

| 类型 | Java / Kotlin 公共能力 |
|---|---|
| BindingInflater<VB> | 三参 inflate 工厂，Java/Kotlin 传 XxxBinding::inflate；Base 统一 attachToParent=false |
| BaseActivity<VB> | 构造传 BindingInflater，仅实现 onBindingCreated(Bundle?)；原 inflateBinding 重写入口保留，getBinding() 为 protected |
| BaseFragment<VB> | 公开无参构造中传 BindingInflater，仅实现 onBindingCreated(Bundle?)；getBinding() 仅在 view 生命周期有效，保留原 inflateBinding 重写入口 |
| BaseDialogFragment | override createContentView(inflater,container)；子类自己管理 Binding 释放 |
| BaseViewModel | getPageState(): LiveData<PageState>、getRequestBusy(): LiveData<Boolean>；protected setters 在 Main 使用 |
| PageState / PageStatus | loading()/content()/empty()/error(@StringRes)；ERROR 不保存原始网络响应 |
| PageStateView | attachContent(view)、setRetryAction(Runnable)、render(PageState) |
| LoadingTextView | setLoading(boolean)、isLoading()、showLoading()/hideLoading()、setLoadingText(CharSequence)、setAutoLoadingOnClick(boolean)，XML 属性和用法见 [LOADING_TEXTVIEW.md](LOADING_TEXTVIEW.md) |
| LoadingDialog | DialogFragment，使用 show(fragmentManager,tag)；支持系统重建 |
| UiFeedback | toast(view,@StringRes)、snackbar(view,@StringRes)；Main |
| Clicks | setDebounced(view,Runnable,intervalMillis=600)，单 View 时间窗口 |
| TextSpans | colorRange(text,start,end,@ColorInt)、colorMatches(text,keyword,@ColorInt)；返回保留已有样式的文本副本，区间为 UTF-16 左闭右开 |
| ViewLayouts | setSize(view,widthPx,heightPx)、setMargins(view,startPx,topPx,endPx,bottomPx)、setBottomPadding(view,bottomPx)、onNextPreDraw(view,Runnable)；Main，保留父容器参数类型，预绘制返回可取消监听器 |
| ListLoadStateAdapter | submitState(PageState)，与业务 ListAdapter 通过官方 ConcatAdapter 组合 |

## 常用颜色与主题

core:ui 提供 27 个支持深色模式的语义颜色和 3 个固定色，包含 color_main_title、color_sub_title、color_body、color_hint、color_background、color_surface、color_divider、color_error 等。Theme.BaseKit 映射默认文字/背景和 Material 品牌色。完整表格及覆盖方法见 [COLORS.md](COLORS.md)。

## System

- Navigation.intent(activity,destination,extras?)、registerResult(ComponentActivity,ActivityResultCallback)，注册必须早于 STARTED。Fragment 直接用官方 registerForActivityResult。
- PermissionManager：基于原生 Activity Result API 的独立权限子类、成功/拒绝回调、解释与永久拒绝弹框、设置返回复查；内置 Camera/Microphone/NotificationPermissionManager。见 [权限封装](PERMISSIONS.md)。Permissions.isGranted/register 保留底层兼容入口。
- NetworkStatus.isOnline(context) 检查 INTERNET + VALIDATED，兼容 VPN；observe(context,listener) 返回 AutoCloseable，Main 投递，离页关闭。
- Keyboard.show/hide(view)，Activity-backed View，用 WindowInsetsControllerCompat。
- Screen.dp(context,float)、sp(context,float)、windowBounds(activity)；bounds 在 View 布局后读取，实际窗口尺寸，不猜测物理屏幕尺寸。
- AppFiles.createTemp(context,suffix)、deleteExpiredTemp(context,maxAgeMillis)；清理只删除 basekit_ 前缀文件，在 IO 调用。
- DeviceInfo.appVersion(context)、sdk()、model()、manufacturer()。业务决定哪些字段可采集，框架不上传。
- ClipboardTools.copyText(context,text,label="",sensitive=false)；Main，用户主动复制，敏感标记控制系统预览，不加密内容。
- SystemIntents.web(url)、shareText(text,subject?)、appSettings(context) 构造 Intent；startIfAvailable(context,intent) 返回是否启动成功，不修改原 Intent。

## 常用工具与类职责

工具按 Common（文本、日期、数字、摘要、时长、字节大小、URL）、UI（点击、反馈、文字着色与布局）、System（屏幕、键盘、文件、设备、剪贴板和系统 Intent）分类列于上文。详细源码链接、调用参考、Java 示例及 Pockly 迁移对照见 [TOOLS](TOOLS.md)。

Base、数据类、接口、内部辅助类及 Sample 的职责和边界已写入源码 KDoc/Javadoc；完整生产类型索引见 [CLASS_GUIDE](CLASS_GUIDE.md)。

## 业务适配点

业务 API 响应封装、业务 code 判断、Token 刷新请求、国际化错误文案、Room 正式数据表、缓存用户 scope、权限解释与导航目的地均由接入 App 实现；这些是依赖倒置边界，不是空占位类。
