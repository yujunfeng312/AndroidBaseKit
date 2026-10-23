# 锁定依赖与兼容性

编写和核验日期：2026-10-14。所有直接版本集中在 gradle/libs.versions.toml，无 +、latest 或动态版本。选用已发布稳定版本，不要求每个库都升级到当天最新版本；以实际构建组合为验收依据。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 版本与用途 | [依赖版本](#依赖版本) | 构建链、SDK、网络、图片、存储、UI 和测试依赖 |
| 编译与 ABI | [处理器与 native 库](#处理器与-native-库) | Room KSP、Glide 处理器取舍与 MMKV ABI 边界 |
| 来源与解析 | [来源与检查](#来源与检查) | 官方来源、依赖解析命令与传递版本冲突 |

## 依赖版本

| 组件 | 锁定版本 | 用途 / 选择原因 |
|---|---|---|
| AGP | 8.11.1 | 按当前指定版本；使用配套的 API 36 |
| Gradle Wrapper | 8.13 | AGP 8.11 官方配套版本，官方 SHA-256 校验 |
| 构建运行 / CI JDK | 21 | 按当前选择，直接使用本机 Android Studio JBR 21；CI 使用 Temurin 21 |
| Java/Kotlin 字节码目标 | 17 | 与运行 Gradle 的 JDK 分开设置；Java compileOptions 和 Kotlin compilerOptions 明确设置为 17 |
| Kotlin Android / stdlib | 2.2.21 | 显式 Kotlin Android 插件，官方兼容表覆盖 AGP 8.11.1 / Gradle 8.13 |
| KSP | 2.2.21-2.0.4 | Kotlin 2.2.21 对应版本，显式 useKSP2=true；原 KSP 2.3.12 要求 AGP >=8.12，不适用于当前配置 |
| Android SDK | min 24 / compile 36 / target 36 | 默认 Build Tools 35.0.0；SDK 包目录 android-36 |
| Retrofit / converter-gson | 3.0.0 | suspend 与 Java Call；与 OkHttp 5 组合实际编译及 MockWebServer 验证 |
| OkHttp / MockWebServer | 5.1.0 | 显式选择 OkHttp 5，覆盖 Retrofit 的传递 OkHttp 4.12；可取消请求/流式 IO |
| Gson | 2.13.2 | Retrofit JSON 转换与持久缓存序列化 |
| Glide | 5.0.5 | 兼容 SDK 36；5.0.9 及其 gifdecoder/disklrucache 要求 compileSdk 37，故使用 5.0.5；保留标准图片/GIF/缓存 API |
| MMKV | 1.3.17 LTS | 保留 32 位与 64 位 ABI 兼容，而非默认要求 64 位的 2.x；实际 ABI 以 AAR 和设备记录为准 |
| Room runtime/ktx/compiler | 2.8.5 | 稳定 Room 2.x，KSP 生成实现和 schema，显式 migration |
| Lifecycle | 2.9.4 | runtime、process、viewmodel、livedata，MVVM 和任务取消 |
| Coroutines | 1.10.2 | android Main/IO、可取消 suspend、测试工具 |
| AppCompat | 1.7.1 | XML Activity 和主题 |
| Activity | 1.11.0 | Activity Result API |
| Fragment | 1.8.9 | Fragment/ViewLifecycleOwner |
| Core KTX | 1.17.0 | Insets、ContextCompat、PackageInfoCompat |
| RecyclerView | 1.4.0 | ListAdapter、DiffUtil、ConcatAdapter |
| SwipeRefreshLayout | 1.2.0 | LoadingTextView 内部复用 CircularProgressDrawable；未公开第三方动画类型 |
| Material | 1.13.0 | XML 主题与 Snackbar |
| Navigation Fragment | 2.9.5 | XML nav graph、NavHostFragment |
| WorkManager | 2.10.5 | CoroutineWorker 示例，固定稳定维护版 |
| Timber | 5.0.1 | Debug 元数据日志；Release 不种植 DebugTree |
| JUnit | 4.13.2 | JVM 测试 |
| Robolectric | 4.16.1 | Java UI 生命周期/Room JVM 测试，使用 API 28 |
| AndroidX Test Core/Runner | 1.7.0 | JVM context、设备 instrumentation |
| AndroidX Test JUnit | 1.3.0 | AndroidJUnit4 |
| Espresso | 3.7.0 | Java 示例设备 UI 测试 |

## 处理器与 native 库

Room Kotlin source 使用 KSP；Room 编译器生成 DemoDatabase_Impl 等真实实现，schema 2.json 在仓库中保存。没有 kapt、allowMainThreadQueries 或 fallbackToDestructiveMigration。

Glide 标准 API 无需 generated GlideApp，本框架未声明自定义 AppGlideModule，所以没有无意义地接入 Glide 注解处理器。若业务需要生成 API/注册自定义组件，可在 Java AppGlideModule 所在模块使用与 Glide 相同版本的 Java annotationProcessor；当前框架保持 KSP/Java annotationProcessor，不添加未使用的 kapt 插件。新增处理器后必须再次编译验证。

MMKV 初始化在 Application，所有公开用户 store 独立命名。设备测试验证 arm64 运行；ABI/16 KB page size 的其它组合应在上线矩阵中继续验证，不由一次 API 28 验证推断。框架不设置 abiFilters 剔除调用方需要的 ABI。

## 来源与检查

- [AGP 8.11 官方兼容表](https://developer.android.com/build/releases/agp-8-11-0-release-notes)
- [Kotlin Gradle 插件兼容表](https://kotlinlang.org/docs/gradle-configure-project.html)
- [Gradle 8.13 发行包与校验](https://services.gradle.org/distributions/gradle-8.13-bin.zip.sha256)
- [KSP 2.2.21-2.0.4](https://github.com/google/ksp/releases/tag/2.2.21-2.0.4)
- [SwipeRefreshLayout 1.2.0](https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout)
- [Room 稳定发行记录](https://developer.android.com/jetpack/androidx/releases/room)
- [Glide 5.0.5](https://github.com/bumptech/glide/releases/tag/v5.0.5)
- [MMKV 1.3.17](https://github.com/Tencent/MMKV/releases/tag/v1.3.17)
- [Retrofit 3.0.0](https://github.com/square/retrofit/releases/tag/3.0.0)
- [OkHttp 5.1.0 Maven POM](https://repo.maven.apache.org/maven2/com/squareup/okhttp3/okhttp/5.1.0/okhttp-5.1.0.pom)

实际依赖解析可运行 `./gradlew :sample:dependencies --configuration debugRuntimeClasspath`。新 App 合并 catalog 时审查 Gradle 传递版本冲突；catalog 固定直接版本并不意味着所有传递依赖都无法被接入 App 升级。
