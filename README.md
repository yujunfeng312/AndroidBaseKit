# AndroidBaseKit

面向多个 Android 商业项目复用的 Kotlin 基础框架，仓库标识 `android-base-kit`，工程名 `AndroidBaseKit`。UI 使用 XML + ViewBinding，架构为 MVVM + Repository，Java 页面可以直接继承 Kotlin Base。没有 Compose、MVP、全局 Activity/Fragment 管理器或巨型 Utils。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 模块与能力 | [模块和依赖](#模块和依赖) | 各 core 模块能力及依赖边界 |
| 环境与运行 | [开始运行](#开始运行) | JDK、SDK、构建命令与 Sample 启动 |
| 原生控件 | [原生 View 图鉴](docs/NATIVE_VIEWS.md) | 分类检索、64 个独立页面与常用属性演示 |
| 系统与窗口 | [系统设置 Demo](docs/SYSTEM_UI_DEMO.md) | 沉浸式、系统栏颜色、窗口亮度、键盘与系统设置入口 |
| Java 接入 | [Java 调用](#java-调用) | Java 页面继承、LiveData、图片和存储调用 |
| Kotlin 接入 | [Kotlin 调用](#kotlin-调用) | 协程请求、结果和取消处理 |
| 跨工程复用 | [接入另一个工程](#接入另一个工程) | 模块导入、Version Catalog 与业务适配 |
| 专题文档 | [文档与验收](#文档与验收) | 架构、API、工具、主题与验证文档入口 |

## 模块和依赖

| 模块 | 能力 | 工程内依赖 |
|---|---|---|
| `core:common` | ApiResult、AppError、分类常量、文本/日期/数字/时长/文件大小/URL/SHA-256 工具 | 无 |
| `core:network` | Retrofit/OkHttp、BaseRequest/BaseResponse、错误映射、受控重试、单次刷新、Java 回调、传输进度 | common |
| `core:image` | Glide 图片、圆角/头像/GIF、缓存和取消 | 无 |
| `core:storage` | MMKV、LRU/TTL 缓存、缓存策略、Room DAO/迁移 | common |
| `core:lifecycle` | 前后台 LiveData、生命周期绑定任务、WorkManager 依赖 | 无 |
| `core:ui` | BaseActivity/Fragment/ViewModel、页面状态、Dialog、列表状态、LoadingTextView、文字着色与布局工具、公共颜色与 DayNight 主题 | common、lifecycle |
| `core:system` | 导航/结果、权限、网络状态、键盘、屏幕、文件、设备信息、剪贴板与系统 Intent | 无 |
| `sample` | Java + XML 示例、Kotlin ViewModel/Repository、本地数据、后台清理任务 | 全部 core |

依赖箭头和设计边界见 [ARCHITECTURE](docs/ARCHITECTURE.md)。

## 开始运行

构建运行 JDK 使用 21（本机可直接使用 Android Studio JBR 21），Java/Kotlin 字节码目标保持 17；需要 Android SDK Platform 36、Build Tools 35.0.0 和可访问 Google Maven/Maven Central 的网络。Gradle Wrapper 固定为 8.13，AGP 为 8.11.1；显式应用 Kotlin Android 2.2.21，Room 使用配套 KSP2 2.2.21-2.0.4。

当前 compileSdk/targetSdk 均使用 36，minSdk 为 24，与指定的 AGP 8.11.1 配套。本机同时安装 android-36、android-36.1、android-37.0，工程选择 android-36。Glide 固定为 5.0.5，避免 5.0.9 及其 gifdecoder/disklrucache 对 compileSdk 37 的要求。

此前 SDK 37 配置虽然通过 Gradle 构建，但 Android Studio 2025.2.3 同步提示 `Could not find compile target android-37`，运行 sample 报 `Please select Android SDK`。已按用户选择改为 SDK 36；诊断与复验见 [验收记录](docs/VALIDATION.md)。

1. Android Studio 打开本目录，配置 Gradle JDK 21（Android Studio 内置 JBR 21）。
2. 在未跟踪的 `local.properties` 中设置 `sdk.dir=/your/android/sdk`，或配置 `ANDROID_HOME`。
3. 执行：

```sh
# macOS / Android Studio JBR 21
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :sample:assembleDebug testDebugUnitTest lintDebug
./gradlew :sample:assembleRelease
./gradlew :sample:installDebug
# 有设备时：
./gradlew :sample:connectedDebugAndroidTest
```

桌面入口为 `dev.androidbasekit.sample.view.activity.SplashActivity`：展示项目启动页约 1 秒后分流。首次为项目启动页 → 四张 PNG 欢迎介绍 → MainActivity，欢迎完成后为项目启动页 → MainActivity；独立 GuideActivity 从项目说明页手动打开。主页按职责分为「示例目录」「工具体验」「项目说明」，每个演示只有一个入口，Tab 点击、滑动与页面重建恢复由 ViewPager2 管理。

- 示例目录：基础模块、[64 类原生 View 图鉴](docs/NATIVE_VIEWS.md)、[系统与窗口设置](docs/SYSTEM_UI_DEMO.md)。
- 工具体验：提示与弹窗、用户主动复制文字、时长/文件大小转换、10 秒 TTL 内存缓存。
- 项目说明：版本与架构介绍、使用边界、重新体验欢迎页、单独查看功能引导。

基础模块按数据操作、页面状态、结果返回和列表分区；默认由 Room + MMKV 提供本地数据，本地图片经 Glide 加载，点击条目进入导航详情。网络刷新调用 JSONPlaceholder，已有内容时仅显示刷新进度，首次加载使用页面加载状态。共享 PageStateView 统一加载、空数据和错误样式。启动模板说明见 [Java 应用模板](docs/JAVA_APP_TEMPLATE.md)。WorkManager 示例可通过 `CacheCleanupWorker.schedule(context)` 按需调度。

Release 产物未配置正式签名；商业项目在自己的 App 中配置签名。不要提交本地 SDK 路径或签名文件。

## Java 调用

```java
public final class MyActivity extends BaseActivity<ActivityResultBinding> {
    public MyActivity() { super(ActivityResultBinding::inflate); }
    @Override protected void onBindingCreated(Bundle savedInstanceState) {
        getBinding().doneButton.setOnClickListener(view -> finish());
    }
}
```

```java
DemoViewModel vm = new ViewModelProvider(this,
    new DemoViewModel.Factory(getApplication())).get(DemoViewModel.class);
vm.getItems().observe(this, adapter::submitList);
vm.getPageState().observe(this, pageStateView::render);
vm.refreshNetwork();
ImageLoader.loadAvatar(imageView, url);
AppPreferences.initialize(getApplication()); // 通常在 Application 中一次初始化
AppPreferences prefs = AppPreferences.open("user-id");
prefs.putString("theme", "dark");
String theme = prefs.getString("theme", "system");
```

Fragment 必须使用 `getViewLifecycleOwner()` 观察数据和绑定 JavaRequests，Binding 仅在 View 生命周期内有效。

## Kotlin 调用

```kotlin
val api = NetworkClient(NetworkConfig("https://example.com/", tokenProvider = TokenProvider { account.token }))
    .create(MyApi::class.java)
viewModelScope.launch {
    when (val result = ApiErrorMapper.execute { api.items() }) {
        is ApiResult.Success -> updateItems(result.data)
        is ApiResult.Failure -> showLocalizedError(result.error.kind)
    }
}
```

取消会传播，不转换成 Failure。UI 页面状态由 ViewModel 决定；已有数据刷新时保留 Content，并单独观察 `requestBusy`。

## 接入另一个工程

最直接方式：复制 `core/` 和 `gradle/libs.versions.toml` 中需要的条目，在目标 `settings.gradle.kts` include 所需模块，并应用相同 AGP/KSP 构建配置。

```kotlin
// settings.gradle.kts
include(":core:common", ":core:network", ":core:image")
// app/build.gradle.kts
implementation(project(":core:network"))
implementation(project(":core:image"))
```

如目录位于外部仓库，可为每个模块配置 `project(":core:network").projectDir = file("../android-base-kit/core/network")` 等路径；目标工程要合并 Version Catalog。`network` 自动引入 common，`ui` 自动引入 common/lifecycle。MMKV 在 Application 初始化；TokenProvider/TokenRefresher、业务 DTO/Repository、正式 Room 数据表、错误文案由接入 App 提供。这个仓库没有发布 Maven 坐标，不应引用虚构的远程版本。团队规模扩大后可增加 Maven publication，而无需改变模块边界。

## 文档与验收

- [Java 启动与主页模板：项目启动页 → Welcome → Main（ViewPager2 + Tab）](docs/JAVA_APP_TEMPLATE.md)
- [MVVM 目录与简化 Binding 创建](docs/MVVM.md)
- [每个类的用途与功能索引](docs/CLASS_GUIDE.md)
- [常用工具分类、Java 示例与 Pockly 迁移对照](docs/TOOLS.md)
- [架构](docs/ARCHITECTURE.md)
- [公共 API](docs/API.md)
- [请求 Base](docs/REQUEST_BASE.md)
- [常用颜色与主题](docs/COLORS.md)
- [LoadingTextView](docs/LOADING_TEXTVIEW.md)
- [依赖版本与兼容性](docs/DEPENDENCIES.md)
- [Java 互操作规范](docs/JAVA_INTEROP.md)
- [实际构建与测试结果](docs/VALIDATION.md)

测试覆盖真实 HTTP 请求、错误转换、重试、刷新并发、传输进度、TTL/LRU、缓存失败、Room 数据隔离/迁移、Java Binding 生命周期及设备 MMKV/UI 流程。详见验收记录，静态编译不能代替所有设备行为验证。
