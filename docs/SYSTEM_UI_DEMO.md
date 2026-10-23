# 系统与窗口设置 Demo

## 入口与能力

主页「示例目录」→「系统与窗口设置」。页面中的选项会立即应用到当前演示窗口，支持一键恢复默认设置。

| 区域 | 可操作内容 |
| --- | --- |
| 系统栏 | 状态栏与导航栏分别显示/隐藏，一键沉浸或恢复显示 |
| 颜色 | 两种系统栏分别选择透明、浅色、靛蓝、深色底色 |
| 图标 | 根据背景自动匹配深浅，或关闭自动匹配后手动指定 |
| 背景 | 渐变背景延伸到系统栏下方；操作区域保持安全边距 |
| 屏幕 | 当前窗口保持亮屏，亮度跟随系统或手动调整 |
| 输入 | 显示/收起软键盘，同时观察内容避让 |
| 系统入口 | 显示设置、当前应用详情与权限、当前应用通知设置 |

界面文字、选项和动态数值使用中文字符串资源。页面重建会恢复当前选项，亮度和亮屏状态不写入手机全局设置。系统设置按钮仅打开相应页面，等待用户自行调整。

## 实现与兼容

- `SystemUiDemoActivity` 使用 `WindowInsetsControllerCompat` 控制图标与可见性；沉浸式允许边缘滑动临时唤出系统栏。
- 当前工程 targetSdk 为 36，窗口按边到边方式处理。旧系统显式设置透明系统栏，新系统由系统约束透明；`SystemBarBackdropView` 根据实际 Insets 在系统栏下绘制底色，避免依赖新系统中失效的旧颜色设置方式。
- 底色层保持全窗口大小；操作内容独立避让系统栏、桌面窗口标题栏、屏幕开孔和软键盘。导航栏位于左右侧时也按对应边距绘制。
- Android 7.x 不支持深色导航栏图标，对应开关不可用。IME、系统临时唤出的系统栏、手势条和多窗口可能由系统调整外观，页面显示实际可见性与边距供对照。
- 设置范围为当前 Demo Activity，不修改其他页面或 core 模块默认系统栏行为。

官方参考：[沉浸式系统栏](https://developer.android.com/develop/ui/views/layout/immersive)、[手动配置边到边](https://developer.android.com/develop/ui/views/layout/edge-to-edge-manually)、[View 边到边适配](https://developer.android.com/develop/ui/views/layout/edge-to-edge)。

## 代码入口

- [SystemUiDemoActivity](../sample/src/main/java/dev/androidbasekit/sample/view/activity/SystemUiDemoActivity.java)
- [SystemBarBackdropView](../sample/src/main/java/dev/androidbasekit/sample/view/widget/SystemBarBackdropView.java)
- [页面 XML](../sample/src/main/res/layout/activity_system_ui_demo.xml)
- [中文文案](../sample/src/main/res/values/system_ui_strings.xml)

静态构建和本地窗口状态测试不等于实际设备效果；系统栏颜色、手势导航、横竖屏、开孔及键盘的最终显示效果留给用户手动查看。本次不运行设备遍历。
