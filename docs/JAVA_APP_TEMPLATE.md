# Java 应用启动与主页模板

此模板位于 `sample`，页面、Adapter、设置存储均使用 Java；页面使用 XML + ViewBinding 并继承现有 Kotlin Base。无需网络即可运行。

## 分类索引

| 分类 | 入口 | 内容 |
| --- | --- | --- |
| 启动流程 | [流程与返回栈](#流程与返回栈) | 首次引导、后续启动、返回行为 |
| 文件职责 | [文件清单](#文件清单) | Activity、Fragment、Adapter、布局 |
| 复制接入 | [接入业务](#接入业务) | 依赖、Manifest、Tab 扩展 |
| 验证 | [运行与验证](#运行与验证) | 编译命令、自动化与设备检查 |

## 流程与返回栈

```text
项目启动页 SplashActivity（展示约 1 秒后分流）
             ├─ 未完成欢迎 → WelcomeActivity（4 张 PNG）→ 点击“开始使用” → MainActivity
             └─ 已完成欢迎 → MainActivity

MainActivity → 项目说明 → 查看功能引导 → GuideActivity → 完成后关闭，返回原主页
```

- Splash 展示项目启动布局约 1 秒后分流并 finish。页面进入后台时取消待执行跳转，返回前台后重新计时。Android 12+ 在项目启动页之前仍会有系统启动画面。
- Welcome 使用 ViewPager2 展示四张本地 PNG 插图与介绍文案，支持滑动、页码和下一页按钮。最后一页点击“开始使用”才保存欢迎完成状态并直接进入主页；未完成时退出，下次仍展示欢迎。
- 欢迎插图位于 `sample/src/main/res/drawable-nodpi/welcome_intro_1.png` 至 `welcome_intro_4.png`，当前为 720 × 540 示意图，可直接替换为产品素材。插图使用 fitCenter 保持比例；文案独立于 PNG，便于本地化与无障碍阅读。
- Guide 保留为独立功能介绍，从项目说明页手动打开，不参与首次或后续启动分流；完成后关闭并返回原主页。
- Main 的三个 Tab 支持点击和左右滑动。使用 `FragmentStateAdapter`，不手动缓存 Fragment/View，也不扩大默认预加载范围。
- Activity 重建时交给 ViewPager2/FragmentManager 恢复当前页与 Fragment，不重新强制选中第一页。Mediator 在销毁时解除绑定。
- 示例目录集中放置基础模块、原生 View 图鉴和系统窗口演示，每个入口只出现一次。工具体验和项目说明使用独立 Fragment。
- 项目说明“重新体验欢迎页”只重置欢迎标记，并清空当前任务返回栈后重新走项目启动页 → Welcome → Main，便于反复验收。
- 重复点击桌面图标产生非任务根 Splash 时结束该入口，保留已有页面。

## 文件清单

以下 Java 链接均指向可直接运行的源码：

| 文件 | 用途 |
| --- | --- |
| [SplashActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/SplashActivity.java) | 桌面入口与首次引导分流 |
| [WelcomeActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/WelcomeActivity.java) | 四张 PNG 介绍、分页控制与欢迎完成状态 |
| [GuideActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/GuideActivity.java) | 独立功能引导与引导完成状态 |
| [WelcomePagerAdapter](../sample/src/main/java/dev/androidbasekit/sample/view/adapter/WelcomePagerAdapter.java) | 四张本地 PNG 与介绍文案 |
| [MainActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/MainActivity.java) | ViewPager2 + TabLayoutMediator |
| [TemplateActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/TemplateActivity.java) | 继承 BaseActivity，统一系统栏/刘海安全区适配 |
| [MainPagerAdapter](../sample/src/main/java/dev/androidbasekit/sample/view/adapter/MainPagerAdapter.java) | 固定 Tab 标题与 Fragment 创建 |
| [MainTabFragment](../sample/src/main/java/dev/androidbasekit/sample/view/fragment/MainTabFragment.java) | 独立示例目录，通过卡片进入三类演示 |
| [ToolsFragment](../sample/src/main/java/dev/androidbasekit/sample/view/fragment/ToolsFragment.java) | 提示弹窗、剪贴板、格式转换和内存缓存体验 |
| [AboutFragment](../sample/src/main/java/dev/androidbasekit/sample/view/fragment/AboutFragment.java) | 版本、架构说明和引导重放 |
| [LaunchPreferences](../sample/src/main/java/dev/androidbasekit/sample/model/repository/LaunchPreferences.java) | 欢迎与引导独立标记，共用专属 SharedPreferences 文件 |
| [项目启动布局](../sample/src/main/res/layout/activity_splash.xml) | 项目启动页品牌内容 |
| [欢迎布局](../sample/src/main/res/layout/activity_welcome.xml) | 欢迎 ViewPager2、页码与按钮 |
| [欢迎单页布局](../sample/src/main/res/layout/item_welcome_page.xml) | PNG 插图、标题和说明 |
| [引导布局](../sample/src/main/res/layout/activity_guide.xml) | 功能引导与开始按钮 |
| [主页布局](../sample/src/main/res/layout/activity_main.xml) | 顶部 Tab + 下方 ViewPager2 |
| [Tab 布局](../sample/src/main/res/layout/fragment_main_tab.xml) | 标题、说明、唯一入口卡片 |

文案放在 `values/template_strings.xml`、`values-zh/template_strings.xml`，尺寸放在 `values/template_dimens.xml`，可跟随应用 DayNight 主题。

## 接入业务

1. 保留 `core:ui`，复制上表 Java 与布局资源、文案和尺寸，替换包名及 Binding/R 导入。
2. App 开启 `buildFeatures.viewBinding = true`，显式依赖 `androidx.viewpager2:viewpager2`；TabLayout/Mediator 使用 `core:ui` 已导出的 Material 依赖。当前版本见 [Version Catalog](../gradle/libs.versions.toml)。
3. 参考 [Manifest](../sample/src/main/AndroidManifest.xml)，只给 Splash 设置 `MAIN/LAUNCHER` 和 `exported=true`，Welcome/Guide/Main 使用 `exported=false`。
4. 替换四张欢迎 PNG、介绍文案和独立引导内容；需要异步初始化时，在生命周期允许导航后分流，不用固定延时模拟初始化。
5. 在 `MainPagerAdapter.createFragment()` 中返回各业务 Fragment，并同步修改标题数组。当前 Adapter 使用固定语义页 ID，已与旧占位页 ID 分离；动态增删/排序 Tab 时同步调整 `getItemId()` 与 `containsItem()`。
6. 各业务 Fragment 使用 `BaseFragment<XxxBinding>` 和公开无参构造，数据通过 Bundle/ViewModel/Repository 传递，观察 UI 使用 `getViewLifecycleOwner()`。
7. 在目录页替换示例入口卡片，并按业务需要调整工具、说明页。若切换为底部 Tab，把主页 XML 中的 TabLayout 移到 ViewPager2 后面即可。

引导标记保存在应用级 `java_launch_template` 文件，独立于用户登录状态。示例使用 Android SharedPreferences，复制启动流程无需增加 MMKV 初始化；若业务统一使用现有 `AppPreferences`，可在该 Repository 内替换存储实现，页面无需变化。`welcome_completed` 表示已完成欢迎介绍，`guide_completed` 表示已完成功能引导；这些标记不表示登录、授权或服务端同意状态。

## 运行与验证

```sh
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew \
  :sample:assembleDebug :sample:testDebugUnitTest :sample:lintDebug
```

[LaunchTemplateTest](../sample/src/test/java/dev/androidbasekit/sample/LaunchTemplateTest.java) 覆盖首次/欢迎已完成分流、四页欢迎与重建恢复、两种完成状态互不影响、连续点击仅导航一次、Tab 与 Pager 双向联动、Activity 重建恢复选中页。

设备验收：首次安装走项目启动页 → 四页欢迎 → Main；欢迎完成后重启走项目启动页 → Main；独立引导仅从项目说明页打开；检查点击与滑动切页、旋转和进程重建、返回退出、打开原 Demo 后返回、重新体验引导，以及 Android 15/16 系统栏与大字体布局。单元测试与静态编译不能代替这些设备表现。

欢迎与引导拆分后验证：`:sample:assembleDebug`、`:sample:testDebugUnitTest`、`:sample:lintDebug` 全部通过，Sample 共 33 个单元测试，失败/错误/跳过均为 0。启动模板回归包含 4 个测试；本次未进行设备验收。

当前主页整理后仍保留三个 Tab；已移除原来的重复占位按钮和专属闲置文案。基础模块页面与共享状态组件使用统一间距、语义颜色与卡片层级。设备遍历按用户要求不运行，视觉、手势、键盘和大字体效果由用户手动查看。
