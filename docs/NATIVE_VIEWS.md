# 原生 View 控件图鉴

## 使用入口

主页「示例目录」→「原生 View 控件图鉴」→ 按类别或搜索类名/属性 → 独立控件页。

目录以分类标题和控件卡片分组，显示每组数量；卡片分开展示中文名称、英文类名和精简属性摘要。搜索仍匹配完整 API 属性名称，详情页保留完整说明。

每种控件使用同一个 Activity 模板承载，但通过稳定类名路由进入独立页面，不将所有控件堆在一个页面中。返回保留目录搜索和滚动状态。

## 页面能力

- 同页比较该控件的常用属性，示例上方标注中文含义、实际属性名和值。
- Demo 的界面文案、目录标题、属性说明、输入提示、无障碍描述和动态数值文案统一使用 `R.string`；列表数据从引用字符串资源的 `string-array` 读取。默认资源与 `values-zh` 同步为中文。
- 首页、首次引导、原有框架示例也改为中文；`TemplateActivity` 仅为 Sample 页面设置中文资源环境，使原生日期、时间及媒体控件同步显示中文。
- 通用面板可实时调节 enabled、alpha（原透明度的百分比）、padding（在原内边距上增加 0～32dp）。
- 重置按钮重新创建示例，恢复属性默认值。
- 输入框、勾选和选中项使用系统 View 状态保存；通用面板属性支持 Activity 重建恢复。
- 原生控件通过 Java 显式构造，避免 inflater 将 Button 等替换为 AppCompat/Material 版本。必须依赖 XML 的旧控件提供专用布局。
- 内置 6 秒 H.264 无声测试片，可离线操作 VideoView/MediaController 的播放、暂停和进度；测试片由 FFmpeg testsrc2 程序生成。
- Chronometer、轮播和视频离开页面时暂停，WebView 在销毁时释放。WebView 仅展示本地 HTML，关闭 JavaScript、文件访问、内容访问及网络加载。

## 覆盖范围

当前共 **64 个独立类型页面**。以 compileSdk 36 中 `android.widget` 顶层公开、可实例化的 View 控件为主体，并增加 `android.view` 的 View、ViewStub、SurfaceView、TextureView 和 WebView。

“所有”限定为控件图鉴中的公开 UI 控件，不包含抽象基类、接口、Adapter、PopupWindow/Toast 等非 View 类型，也不包括需特定系统宿主的输入法视图、AppWidget、自动填充 InlineContentView，以及 OpenGL、手势绘制等专用渲染组件。AndroidX/Material 不属于原生 framework 控件，本次未混入目录。

SurfaceView/TextureView 展示真实容器及生命周期说明，未附加相机或解码渲染器；QuickContactBadge 展示真实外观，未绑定实际联系人，因此不读取通讯录。Legacy 页面保留旧控件，供维护旧工程时对照，不代表推荐使用。

## 分类索引

| 类别 | 控件 | 页面内属性 |
| --- | --- | --- |
| 文本与输入 | `TextView` | textSize / textColor / maxLines / ellipsize / gravity |
| 文本与输入 | `EditText` | inputType / hint / maxLength / singleLine |
| 文本与输入 | `AutoCompleteTextView` | threshold / completionHint / dropDownWidth |
| 文本与输入 | `MultiAutoCompleteTextView` | CommaTokenizer / threshold |
| 文本与输入 | `SearchView` | iconified / queryHint / submitButtonEnabled |
| 文本与输入 | `CheckedTextView` | checked / checkMarkDrawable |
| 按钮与选择 | `Button` | textAllCaps / compoundDrawables / enabled |
| 按钮与选择 | `ImageButton` | src / scaleType / backgroundTint |
| 按钮与选择 | `CheckBox` | checked / buttonTint / text |
| 按钮与选择 | `RadioButton` | checked / buttonTint |
| 按钮与选择 | `RadioGroup` | orientation / checkedButton |
| 按钮与选择 | `Switch` | showText / textOn / textOff / checked |
| 按钮与选择 | `ToggleButton` | textOn / textOff / checked |
| 按钮与选择 | `Spinner` | MODE_DROPDOWN / MODE_DIALOG / selection |
| 按钮与选择 | `NumberPicker` | minValue / maxValue / wrapSelectorWheel / displayedValues |
| 进度与评分 | `ProgressBar` | indeterminate / progress / secondaryProgress |
| 进度与评分 | `SeekBar` | max / progress / keyProgressIncrement |
| 进度与评分 | `RatingBar` | numStars / stepSize / isIndicator |
| 图片与媒体 | `ImageView` | scaleType / adjustViewBounds / tint |
| 图片与媒体 | `ImageSwitcher` | factory / inAnimation / outAnimation |
| 图片与媒体 | `QuickContactBadge` | imageResource / mode / 联系人宿主说明 |
| 图片与媒体 | `VideoView` | MediaController / 本地视频 / start / pause / seekTo |
| 图片与媒体 | `MediaController` | anchorView / show / hide |
| 图片与媒体 | `SurfaceView` | holder / 渲染生命周期说明 |
| 图片与媒体 | `TextureView` | opaque / alpha / 渲染生命周期说明 |
| 日期与时间 | `DatePicker` | calendar / spinner / minDate / maxDate |
| 日期与时间 | `TimePicker` | spinner / clock / is24HourView |
| 日期与时间 | `CalendarView` | firstDayOfWeek / minDate / maxDate |
| 日期与时间 | `Chronometer` | base / format / start / stop |
| 日期与时间 | `TextClock` | format12Hour / format24Hour / timeZone |
| 布局容器 | `View` | background / alpha / rotation |
| 布局容器 | `LinearLayout` | orientation / weight / gravity |
| 布局容器 | `FrameLayout` | layout_gravity / foreground |
| 布局容器 | `RelativeLayout` | CENTER_IN_PARENT / ALIGN_PARENT_END |
| 布局容器 | `TableLayout` | stretchColumns / shrinkColumns / TableRow |
| 布局容器 | `TableRow` | column / span / weight |
| 布局容器 | `GridLayout` | columnCount / rowCount / alignmentMode |
| 布局容器 | `Space` | width / height / 占位间距 |
| 布局容器 | `ViewStub` | layoutResource / inflate / 延迟加载 |
| 滚动与列表 | `ScrollView` | fillViewport / scrollbarFadingEnabled |
| 滚动与列表 | `HorizontalScrollView` | fillViewport / horizontalScrollBarEnabled |
| 滚动与列表 | `ListView` | choiceMode / dividerHeight / header |
| 滚动与列表 | `GridView` | numColumns / spacing / stretchMode |
| 滚动与列表 | `ExpandableListView` | group / child / expandGroup |
| 滚动与列表 | `StackView` | adapter / displayedChild / showNext |
| 切换与工具栏 | `AdapterViewFlipper` | adapter / flipInterval / autoStart |
| 切换与工具栏 | `ViewAnimator` | displayedChild / animateFirstView |
| 切换与工具栏 | `ViewFlipper` | flipInterval / startFlipping / stopFlipping |
| 切换与工具栏 | `ViewSwitcher` | showNext / showPrevious |
| 切换与工具栏 | `TextSwitcher` | factory / setText / 动画 |
| 切换与工具栏 | `Toolbar` | title / subtitle / navigationIcon / menu |
| 切换与工具栏 | `ActionMenuView` | menu / showAsAction / 点击回调 |
| 网页 | `WebView` | 本地 HTML / textZoom / JavaScript 默认关闭 |
| Legacy 旧版控件 | `TabHost` | setup / TabWidget / tabContent |
| Legacy 旧版控件 | `TabWidget` | orientation / stripEnabled |
| Legacy 旧版控件 | `Gallery` | spacing / selection |
| Legacy 旧版控件 | `SlidingDrawer` | orientation / handle / content |
| Legacy 旧版控件 | `AbsoluteLayout` | LayoutParams.x / y |
| Legacy 旧版控件 | `AnalogClock` | 系统默认表盘 |
| Legacy 旧版控件 | `DigitalClock` | textSize / textColor |
| Legacy 旧版控件 | `ZoomButton` | zoomSpeed / longClick / enabled |
| Legacy 旧版控件 | `ZoomControls` | zoomSpeed / isZoomInEnabled / isZoomOutEnabled |
| Legacy 旧版控件 | `TwoLineListItem` | text1 / text2 / 两行布局 |
| Legacy 旧版控件 | `DialerFilter` | mode / digits / letters |

## 代码入口

- [NativeViewsActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/NativeViewsActivity.java)：目录、搜索和页面路由。
- [NativeViewDemoActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/NativeViewDemoActivity.java)：通用属性面板、示例呈现与状态保存。
- [NativeViewCatalog](../sample/src/main/java/dev/androidbasekit/sample/view/nativeviews/NativeViewCatalog.java)：分类与属性清单。
- [NativeViewExamples](../sample/src/main/java/dev/androidbasekit/sample/view/nativeviews/NativeViewExamples.java)：各控件的真实实例、专属属性和交互。
- [NativeViewGalleryTest](../sample/src/androidTest/java/dev/androidbasekit/sample/NativeViewGalleryTest.java)：设备遍历、搜索、属性与输入恢复回归。

扩展控件时同步增加 Catalog 项与 Examples 分支。设备测试会检查每条路由是否包含对应 framework 类型。

## 本次验证

- Debug 构建与 Sample Lint 通过；Lint 为 0 errors，24 warnings，包含原有告警以及原生 Switch、旧控件布局、语言环境和日期格式等提示；无硬编码界面文案告警。
- Sample JVM 单元测试 30 项通过。
- 图鉴初版在 Pixel 7 / Android 17 完成 64 页遍历及全部 3 项设备回归。
- 本次中文化后的设备复验：中文/英文类名搜索、输入与通用属性重建恢复两项通过；完整页面遍历遇到系统返回按键，Activity 被关闭，本次未完成全量设备复验。
- 未在 Android 7.0～16 的设备上逐版本验证；SurfaceView/TextureView 未验证实际渲染管线，QuickContactBadge 未验证真实联系人跳转。
