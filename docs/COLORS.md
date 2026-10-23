# 常用颜色与主题

颜色集中在 core:ui 的 values/colors.xml，values-night/colors.xml 提供对应深色值。名称按用途定义，页面不要写死十六进制颜色。App 可以通过同名资源覆盖品牌色；覆盖时同步检查深色模式及相应 on-color。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 颜色资源 | [颜色资源](#颜色资源) | 品牌色、文字、背景、边框、图标、状态、遮罩与固定色 |
| XML 与主题 | [XML 使用](#xml-使用) | 控件引用、DayNight 主题及默认资源映射 |
| 代码取色 | [Java / Kotlin 使用](#java--kotlin-使用) | ContextCompat、模块 R 与当前主题 Context |
| 品牌适配 | [品牌覆盖](#品牌覆盖) | App 同名资源覆盖及深色模式同步 |

## 颜色资源

| 资源名 | 用途 | 浅色 | 深色 |
|---|---|---|---|
| `color_primary` | 品牌主色、主要操作 | `#3F51B5` | `#BBC3FF` |
| `color_primary_variant` | 主色强调 | `#303F9F` | `#939FFF` |
| `color_on_primary` | 主色背景上的文字/图标 | `#FFFFFF` | `#14206B` |
| `color_secondary` | 辅助操作 | `#006C67` | `#80D5CD` |
| `color_on_secondary` | 辅助色背景上的文字/图标 | `#FFFFFF` | `#003734` |
| `color_main_title` | 主要标题 | `#212121` | `#F1F1F1` |
| `color_sub_title` | 次级标题、辅助说明 | `#616161` | `#C6C6C6` |
| `color_body` | 正文 | `#424242` | `#E0E0E0` |
| `color_hint` | 输入提示、占位说明 | `#757575` | `#AEAEAE` |
| `color_disabled` | 禁用文字/图标 | `#9E9E9E` | `#777777` |
| `color_link` | 文字链接 | `#3046A4` | `#BBC3FF` |
| `color_background` | 页面背景 | `#F5F5F5` | `#121212` |
| `color_surface` | 卡片、列表、弹窗表面 | `#FFFFFF` | `#1E1E1E` |
| `color_background_secondary` | 次级背景、占位区域 | `#EEEEEE` | `#292929` |
| `color_divider` | 分割线 | `#E0E0E0` | `#383838` |
| `color_border` | 边框 | `#BDBDBD` | `#666666` |
| `color_icon` | 常规图标 | `#616161` | `#C6C6C6` |
| `color_icon_secondary` | 次级图标 | `#757575` | `#AEAEAE` |
| `color_success` | 成功状态文字/图标 | `#1B6B35` | `#7FD99E` |
| `color_success_background` | 成功状态背景 | `#E8F5E9` | `#113820` |
| `color_warning` | 警告状态文字/图标 | `#895000` | `#FFB95C` |
| `color_warning_background` | 警告状态背景 | `#FFF3E0` | `#3B280D` |
| `color_error` | 失败/错误/危险操作 | `#B3261E` | `#FFB4AB` |
| `color_error_background` | 错误状态背景 | `#FCE9E7` | `#410E0B` |
| `color_info` | 提示状态文字/图标 | `#005B9F` | `#9ACBFF` |
| `color_info_background` | 提示状态背景 | `#E3F2FD` | `#102D47` |
| `color_scrim` | 遮罩 | `#66000000` | `#99000000` |

另有 color_white、color_black、color_transparent 三个固定颜色，两种模式保持不变。标题、正文、背景优先使用语义名称，而不是固定黑白。

## XML 使用

```xml
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textColor="@color/color_main_title" />
```

Theme.BaseKit 继承 MaterialComponents.DayNight.NoActionBar，映射品牌色、表面、错误色、默认文字色和窗口背景。Sample 的 Theme.AndroidBaseKit 已继承这个主题，标题/正文/占位图也使用公共色。

## Java / Kotlin 使用

```java
int titleColor = ContextCompat.getColor(context,
    dev.androidbasekit.ui.R.color.color_main_title);
textView.setTextColor(titleColor);
```

```kotlin
textView.setTextColor(ContextCompat.getColor(context,
    dev.androidbasekit.ui.R.color.color_main_title))
```

开启 nonTransitiveRClass 时，Java/Kotlin 引用 core:ui 的 R；XML 仍可直接使用合并后的 @color 名称。使用页面的主题 Context 解析颜色，不在单例中缓存颜色整数，以免深色模式切换后继续使用旧值。

## 品牌覆盖

在 App 的 values/colors.xml 和 values-night/colors.xml 分别定义同名 color_primary/color_on_primary 等资源。仅覆盖 values 的主色，而忽略 values-night，会在深色模式下继续使用库的默认主色。

现有 MaterialComponents App 也可以在自己的主题映射这些资源，无需强制更换父主题。运行时主题切换沿用官方 AppCompatDelegate，Base 不增加全局主题管理器。
