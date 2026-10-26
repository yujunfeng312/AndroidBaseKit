# 原生权限 Manager

`core:system` 使用 AndroidX Activity Result API 自行实现运行时权限申请，不依赖第三方权限库。业务入口为 `dev.androidbasekit.system.permission`；`Permissions` 保留底层查询/注册兼容入口。

## 源码分析与流程

参考的是 PermissionX 的流程设计，代码为本项目独立实现：

- [RequestNormalPermissions](https://github.com/guolindev/PermissionX/blob/master/permissionx/src/main/java/com/permissionx/guolindev/request/RequestNormalPermissions.kt)：先检查已授权项，可选先说明，确认后再申请。
- [InvisibleFragment](https://github.com/guolindev/PermissionX/blob/master/permissionx/src/main/java/com/permissionx/guolindev/request/InvisibleFragment.kt)：无界面 Fragment 注册 launcher，结果返回后区分可再次申请与需设置的权限，混合拒绝时优先解释可再次申请项。

本项目实现：

```text
查询当前授权 → 全部允许 → 成功回调
     ↓ 未允许
可选申请前说明 → 取消 → 拒绝回调
     ↓ 确认
原生 RequestMultiplePermissions
     ├─ 全部允许 → 成功回调
     ├─ 可再次申请 → 解释弹框 → 确认重新申请 / 取消结束
     └─ 系统不再弹框 → 设置引导 → 确认前往设置 / 取消结束
                                      ↓ 返回
                              重新查询全部权限 → 最终回调
```

`PermissionFlow` 管理状态、拒绝分类和一次性决策；`PermissionRequestFragment` 接收原生权限/设置页结果；`NativePermissionSession` 绑定页面生命周期与回调；`PermissionPrompt` 管理可替换弹框。设置返回只复查授权，不根据 resultCode 判定，也不自动再次弹框循环。

## 每种权限独立继承

内置 `CameraPermissionManager`、`MicrophonePermissionManager`、`NotificationPermissionManager`，均可继续继承；业务也可直接继承 `PermissionManager`。

```java
public final class ContactsPermissionManager extends PermissionManager {
    @Override protected java.util.List<String> permissions(android.content.Context context) {
        return java.util.Collections.singletonList(android.Manifest.permission.READ_CONTACTS);
    }

    @Override protected PermissionDialog explanationDialog(android.content.Context context) {
        return new PermissionDialog(context.getString(R.string.contacts_reason),
                context.getString(R.string.continue_request), context.getString(R.string.cancel));
    }
}
```

`permissions(Context)` 声明独立权限或不可拆分的权限组，也负责 Android 版本适配；无需申请的版本返回空列表。覆盖 `explanationDialog(Context)`、`permanentDenialDialog(Context)` 设置各 Manager 默认文案；单次请求可通过 options 覆盖。

## Java 调用

```java
CameraPermissionManager camera = new CameraPermissionManager();
boolean started = camera.request(this, new PermissionOptions()
        .explainBeforeRequest()
        .onGranted(granted -> takePhoto())
        .onDenied((granted, denied) -> showPermissionUnavailable()));
```

`this` 支持 FragmentActivity 或 AndroidX Fragment，无需页面提前注册 launcher。Sample「工具体验 → 相机权限申请流程」演示原生说明弹框、权限申请和结果提示。

- `onGranted`：本次权限全部允许；每次请求最多一次。
- `onDenied`：返回已允许和未允许列表；部分允许、取消说明或设置引导也走该回调。
- `explainBeforeRequest()`：申请前先说明；默认直接申请，系统拒绝且允许再次申请时再解释。
- `explanationDialog(PermissionDialog)`、`permanentDenialDialog(PermissionDialog)`：覆盖当前请求弹框文案和按钮文案。
- `onExplain((prompt, permissions, beforeRequest) -> ...)`、`onForwardToSettings((prompt, permissions) -> ...)`：接入自己的 UI。

## 自定义弹框

无需继承框架 Dialog，可以直接用业务已有的 AlertDialog：

```java
new PermissionOptions()
    .onExplain((prompt, permissions, before) -> {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
            .setMessage(R.string.camera_reason)
            .setPositiveButton(R.string.continue_request, (d, which) -> prompt.proceed())
            .setNegativeButton(R.string.cancel, (d, which) -> prompt.cancel())
            .create();
        prompt.attachDialog(dialog);
        dialog.show();
    })
    .onForwardToSettings((prompt, permissions) ->
        prompt.showDialog(new PermissionDialog(getString(R.string.camera_settings_reason),
            getString(R.string.open_settings), getString(R.string.cancel))));
```

`proceed()` 继续申请或打开设置，`cancel()` 结束申请。`attachDialog()` 在 show 前绑定，负责页面销毁关闭弹框，外部点击、返回键和直接 dismiss 统一按取消处理。它会设置取消/关闭监听器，接入后不要覆盖这些监听器。同一 prompt 只能绑定一个弹框，按钮和旧回调重复调用只生效一次。

自定义处理器也可异步等待用户决策，但必须最终调用 proceed/cancel；若展示 Dialog，应先 attachDialog。页面销毁后旧 prompt 自动失效，不会继续申请。

## 生命周期、拒绝分类与边界

主线程、宿主至少 STARTED 且 FragmentManager 未保存状态时才可申请，否则抛 IllegalStateException。同一 Activity 同时只允许一个请求，重复点击返回 false，不冒充权限拒绝；连续申请可在前一次完成回调中启动。

Fragment 请求绑定当前 View 生命周期，无 View 时绑定 Fragment。销毁后清除业务回调、关闭已绑定弹框；系统申请/设置页正在进行时保留请求位，等结果被消费后释放，避免新申请接收旧结果。Activity 销毁不恢复旧 lambda；重建的 launcher 仅消费旧结果，业务由新页面按用户操作重新发起。

只在本次系统申请明确返回拒绝后，结合 `shouldShowRequestPermissionRationale` 分类。不在首次申请前用 rationale=false 判定永久拒绝；空结果/取消不会误弹永久拒绝框。这是系统是否允许再次弹框的判断，设备策略等情况也可能返回 false，不能保证用户去设置就能解除限制。混合拒绝先解释可再次申请项，永久拒绝项保留到后续设置引导；取消则结束当前请求。最终未允许列表不等同于永久拒绝列表。

App 需在 Manifest 声明实际功能权限，库不自动添加相机/录音/通知权限。缺少声明或传入非运行时权限会直接报配置错误。Sample 只声明相机并标记硬件为非必需。通知 Manager 仅处理 Android 13+ POST_NOTIFICATIONS；旧版无需运行时申请，不表示系统通知总开关/渠道已开启。

当前封装针对普通运行时权限；悬浮窗、所有文件访问、安装未知应用等特殊设置权限不能传入该入口。定位、媒体部分访问、蓝牙等场景需业务按 Android 版本和操作顺序拆分 Manager，不直接批量替换权限名。

状态机测试覆盖普通/永久/混合拒绝、取消、重试、设置返回复查和旧决策失效；Robolectric 集成测试验证原生 launcher、弹框和生命周期。系统权限框与厂商行为仍需真机验证。
