# MVVM 目录与 ViewBinding 基类

日期：2026-10-17。Sample 使用真实的 package 目录区分 View、ViewModel、Model；可复用能力继续保留在 core 各模块。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 分层目录 | [目录](#目录) | View、ViewModel、Model 与 Worker 所在位置 |
| 默认 Binding 创建 | [把重复 inflate 收进 Base](#把重复-inflate-收进-base) | Java/Kotlin Binding 工厂与 Fragment 重建约定 |
| 自定义创建与回归 | [兼容自定义布局](#兼容自定义布局) | 旧 inflateBinding 入口及 Binding 生命周期测试 |

## 目录

以下目录均位于 `sample/src/main`；Java 和 Kotlin 目录属于同一个 Android 模块。

```text
java/dev/androidbasekit/sample/
├── DemoApplication.java
└── view/
    ├── activity/       SplashActivity、WelcomeActivity、GuideActivity、MainActivity、TemplateActivity、DemoActivity、ResultActivity
    ├── fragment/       MainTabFragment、ToolsFragment、AboutFragment、DemoFragment、DetailsFragment
    └── adapter/        MainPagerAdapter、DemoAdapter

kotlin/dev/androidbasekit/sample/
├── viewmodel/          DemoViewModel
├── model/
│   ├── api/            DemoApi
│   ├── entity/         Photo
│   └── repository/     DemoRepository
└── worker/             CacheCleanupWorker
```

View 绑定布局、观察状态、处理点击；ViewModel 协调请求和页面状态；Repository 组合 API、Room、MMKV 与缓存；API 声明 Retrofit 接口，entity 保存响应 DTO。Worker 是后台任务入口，独立于页面生命周期。Room 的可复用示例实体/DAO 仍位于 `core:storage`，不创建重复表。

迁移同步更新 Manifest、Navigation 类名、R/BuildConfig 导入、Java/Kotlin 测试引用和 R8 的 Photo/Worker 保留规则。namespace/applicationId、生成的 `sample.databinding` 包名保持不变。

新增 Java 启动模板的应用级引导存储位于 `java/.../model/repository/LaunchPreferences.java`；流程与文件说明见 [Java 应用模板](JAVA_APP_TEMPLATE.md)。

## 把重复 inflate 收进 Base

`BaseActivity`、`BaseFragment` 接收 `BindingInflater<VB>` 工厂。生成的每种 Binding 都提供静态 inflate 方法，因此 Java 页面只需在无参构造中传方法引用。

```java
public final class DemoFragment extends BaseFragment<FragmentDemoBinding> {
    public DemoFragment() {
        super(FragmentDemoBinding::inflate);
    }

    @Override protected void onBindingCreated(Bundle state) {
        // 直接使用 getBinding()，无需再重写 inflateBinding。
    }
}
```

Activity 同样使用 `super(ActivityDemoBinding::inflate)`。Kotlin 可写：

```kotlin
class MyFragment : BaseFragment<FragmentDemoBinding>(FragmentDemoBinding::inflate) {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        // binding 只在当前视图生命周期内使用。
    }
}
```

基类统一调用三参 inflate：Activity 使用 `parent=null`，Fragment 使用当前 container，两者均 `attachToParent=false`。根 View 由 Activity 或 FragmentManager 挂载；Fragment 在 `onDestroyView()` 清空 Binding，重建时创建新 Binding。

这一步仍需指定具体 Binding 工厂，因为 Kotlin/Java 泛型不能直接静态调用 `VB.inflate`。传方法引用可让编译器检查类型，也无需反射和额外 R8 keep 规则。

具体 Fragment 保持公开无参构造，构造内部调用 super 传静态方法引用，支持系统通过 FragmentFactory 重建。不要把 Binding 实例或捕获旧 View 的 lambda 放入工厂。

## 兼容自定义布局

原有继承代码可以继续调用 Base 无参构造，并重写 `inflateBinding`；需要自定义 inflate、bind 已有 View 或特殊布局时仍可使用此入口。若既不传工厂又不重写，启动时明确报错。Sample 四个 Activity/Fragment 已统一改用工厂。

JavaInteropTest 保留原来手动重写的测试，同时增加工厂 Activity/Fragment 的重建测试：检查父容器挂载、重建生成新 Binding、旧视图 Binding 释放以及移除 Fragment 后禁止访问 Binding。
