# LoadingTextView

core:ui 的自定义 AppCompatTextView，适合提交、登录、重试、请求按钮等“点击后 Loading”的场景。使用 AndroidX CircularProgressDrawable，不自行实现动画定时器或请求管理。

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 控件行为 | [能力](#能力) | 点击、加载、恢复、动画生命周期和状态来源 |
| 布局接入 | [XML](#xml) | XML 声明与资源配置示例 |
| Java 与手动状态 | [Java](#java) | 点击监听、LiveData 驱动与防抖组合 |
| Kotlin 与生命周期 | [Kotlin](#kotlin) | 状态观察、加载文案及主线程约束 |
| 属性速查 | [XML 属性](#xml-属性) | 加载文字、指示器尺寸/间距/颜色与自动模式 |

## 能力

- 默认点击进入 Loading，先改变状态再执行标准 OnClickListener，加载中拒绝重复点击。
- 支持自定义加载文字、指示器颜色/大小/间距，以及从 ViewModel 手动驱动。
- 加载结束恢复普通文字、启用状态、compound drawable、间距、原 minimumWidth 和无障碍状态说明；已布局的 wrap_content 控件加载时保留最低宽度。
- 加载期间 setText 更新的是结束后文字；setEnabled 更新的是结束后期望状态，不开放加载中的点击。
- 隐藏、窗口不可见或移出窗口时停止动画，重新显示且仍在加载时恢复动画；不保存 Activity/Fragment 全局引用。
- Loading 是 UI 状态，请求成功、失败、取消都应让业务调用 setLoading(false)。控件不会启动网络、自动超时或把请求状态永久保存到进程恢复状态。

## XML

```xml
<dev.androidbasekit.ui.LoadingTextView
    android:id="@+id/submit"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:gravity="center"
    android:text="@string/submit"
    android:textColor="@color/color_primary"
    app:ltv_loadingText="@string/basekit_loading"
    app:ltv_indicatorSize="18dp"
    app:ltv_indicatorPadding="8dp"
    app:ltv_indicatorColor="@color/color_primary"
    app:ltv_autoLoadingOnClick="true" />
```

父布局声明 xmlns:app="http://schemas.android.com/apk/res-auto"。加载文案使用 strings.xml；缺省文案已有中英文。

## Java

```java
binding.submit.setOnClickListener(view -> {
    // 默认 autoLoadingOnClick=true，回调进入时已经显示 Loading。
    viewModel.submit();
});
// 完成、失败或取消后：
binding.submit.setLoading(false);
// 或 showLoading()/hideLoading()
```

配置 ltv_autoLoadingOnClick=false 后可以完全由 LiveData 驱动：

```java
binding.submit.setAutoLoadingOnClick(false);
viewModel.getRequestBusy().observe(getViewLifecycleOwner(), binding.submit::setLoading);
binding.submit.setOnClickListener(view -> viewModel.submit());
```

Sample 将“本地数据”按钮替换为 LoadingTextView，关闭自动模式，由现有 ViewModel requestBusy 驱动，因此初始加载/刷新/错误/取消都能同步恢复。已有的 Clicks.setDebounced 保持有效。

自动模式应直接使用 OnClickListener；如果组合会忽略某次点击的 debounce/throttle Listener，应使用 LiveData 手动模式，避免控件已 Loading 而业务没有真正启动。

## Kotlin

```kotlin
binding.submit.autoLoadingOnClick = false
viewModel.requestBusy.observe(viewLifecycleOwner) { binding.submit.setLoading(it) }
binding.submit.setOnClickListener { viewModel.submit() }
binding.submit.loadingText = getString(R.string.basekit_loading)
```

所有 View 状态操作在 Main 执行。Fragment 按 ViewLifecycleOwner 观察；配置变更由 ViewModel 状态驱动新 View，控件不恢复一个已不存在的网络任务。

## XML 属性

| 属性 | 默认 / 说明 |
|---|---|
| ltv_loadingText | basekit_loading 文案，可设空文字仅显示指示器 |
| ltv_indicatorSize | 18dp，必须大于 0 |
| ltv_indicatorPadding | 8dp，必须非负 |
| ltv_indicatorColor | 进入 Loading 时的当前文字颜色，或显式颜色资源 |
| ltv_autoLoadingOnClick | true；false 时由调用方 setLoading 控制 |
