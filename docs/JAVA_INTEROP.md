# Kotlin Base 与 Java UI

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 调用约定 | [使用规则](#使用规则) | 泛型、getter、静态入口、默认参数、空值和线程 |
| 页面与 Binding | [Activity 与 Fragment](#activity-与-fragment) | Java 页面创建、LiveData 观察和 Binding 释放 |
| 请求与响应 | [请求 Base 与响应封装](#请求-base-与响应封装) | Java enqueue、响应成功码和状态观察 |
| 取消与失败 | [取消与异常](#取消与异常) | Call 所有权、取消语义和持久化异常 |
| 验证依据 | [实际验证](#实际验证) | Java 编译、Robolectric 与设备测试覆盖入口 |

## 使用规则

1. Base 用 Kotlin；Activity/Fragment/Adapter 可以用 Java。继承泛型 Base 后，在公开无参构造中 `super(XxxBinding::inflate)`，只实现 onBindingCreated；特殊布局仍可重写 inflateBinding。
2. protected getBinding() 来自 Kotlin property getter，不是 Java 可见字段。Fragment onDestroyView 后读取会抛 IllegalStateException。
3. 用 ViewModelProvider.Factory 注入 Repository/Application；LiveData 观察者在 UI 主线程创建，Fragment 使用 getViewLifecycleOwner。
4. Java 通过 ViewModel 普通方法发起 suspend 工作，或使用 JavaRequests + Retrofit Call + ResultCallback。不要从 Java 调 suspend 方法，不手写 Continuation。
5. 标为 @JvmStatic 的 object/companion 方法直接 Class.method()；const val/@JvmField 直接引用字段。有限默认参数使用 @JvmOverloads；复杂 Kotlin data class 可用完整构造函数。复制或变更配置时构造新对象。
6. Kotlin 可空值保留语义：ApiResult.getOrNull/errorOrNull、图片 model、TokenProvider 的 token 都允许 null；非空 callbacks/keys/values 不能传 null。
7. Java 异步回调 onSuccess/onError 在 Main，ProgressListener 在传输线程；不要把传输进度直接写入 View。
8. 用户文案用 strings.xml；AppError.technicalCode 用于诊断或业务映射，禁止直接展示服务器 body/exception。

## Activity 与 Fragment

完整 Java 例子位于 sample 的 view/activity、view/fragment、view/adapter；ViewModel 位于 viewmodel，API/DTO/Repository 位于 model。Activity/Fragment 传入 `XxxBinding::inflate`，基类统一调用三参 inflate、attachToParent=false；不需要每个页面重复重写方法。具体 Fragment 保留公开无参构造以支持系统重建，详细目录与示例见 [MVVM](MVVM.md)。

```java
DemoViewModel vm = new ViewModelProvider(this,
    new DemoViewModel.Factory(requireActivity().getApplication())).get(DemoViewModel.class);
vm.getItems().observe(getViewLifecycleOwner(), adapter::submitList);
vm.getPageState().observe(getViewLifecycleOwner(), state -> getBinding().pageState.render(state));
vm.getRequestBusy().observe(getViewLifecycleOwner(), busy -> {
    // 已有内容时更新刷新指示，不切换为全屏 Loading。
});
```

Fragment 在 super.onDestroyView() 之前解除 RecyclerView adapter，移除 adapter 捕获的页面回调；Base 随后释放 Binding。不要把 ViewBinding 放在 ViewModel、Application、静态单例中。

## 请求 Base 与响应封装

Java 也可以使用 BaseRequest.forOwner(owner)、enqueue(Call<T>)、enqueueResponse(Call<BaseResponse<T>>,successCode)，通过 getState() 的 LiveData 观察 RequestStatus。完整调用示例见 [REQUEST_BASE.md](REQUEST_BASE.md)。BaseResponse.checkSuccess/requireData 提供默认成功码 0 的 Java 重载；非 0 成功码必须显式传入，缺少 code 不当作成功。

## 取消与异常

JavaRequests.enqueue 要在 Main 注册，owner 销毁时调用 Call.cancel 并取消协程；主动取消使用 CancelHandle.cancel。取消不触发 onError。每次请求必须创建新 Call，多个页面不要共享同一个 Call 实例。Kotlin suspend 请求通过 viewModelScope/lifecycleScope 取消。

缓存持久化可能抛 CacheException；在 Repository IO 层处理并向 ViewModel 提交 AppError 或业务事件。Room suspend DAO 不直接暴露给 Java UI，Java 可以观察 DAO 的 LiveData，写入由 Kotlin Repository 执行。

## 实际验证

JavaInteropTest 实际编译了泛型继承、默认构造函数、静态 API、ResultCallback、MemoryCache、PageState、NetworkConfig。Robolectric 测试执行 Java Activity/Fragment 的 Binding 创建/释放和 owner 销毁时的请求取消。DeviceSmokeTest 验证 Java 页面、MMKV 原生读写和 Activity Result。执行状态、失败及覆盖边界以 VALIDATION.md 为准。
