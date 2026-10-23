# 请求 Base

## 分类索引

| 分类 | 章节入口 | 内容 |
| --- | --- | --- |
| 职责划分 | [两层职责](#两层职责) | 服务器响应、请求控制器与客户端结果的区别 |
| 响应协议 | [BaseResponse](#baseresponse) | 成功码、缺失字段、解包及 DTO 适配 |
| Kotlin 调用 | [Kotlin ViewModel](#kotlin-viewmodel) | 作用域、请求覆盖、状态和取消传播 |
| Java 调用 | [Java UI](#java-ui) | LiveData、Retrofit Call、生命周期与取消句柄 |
| 接入边界 | [不变的边界](#不变的边界) | 认证重试、页面状态和缓存分别由哪层管理 |

## 两层职责

BaseResponse<T> 表示可选的 code/message/data 服务端响应约定；BaseRequest<T> 执行请求，提供 LiveData<RequestState<T>>、取消和统一错误转换。ApiResult<T> 是客户端结果，不等同于服务器响应体。普通数组/对象接口继续直接使用 Retrofit，无需强行套 BaseResponse。

## BaseResponse

```kotlin
interface AccountApi {
    @GET("profile") suspend fun profile(): BaseResponse<Profile>
    @GET("profile") fun profileCall(): Call<BaseResponse<Profile>>
}
val profile = api.profile().requireData(successCode = 200)
```

默认成功码为 0，调用方必须按真实服务端协议传入成功码。缺少 code 是解析错误；非成功 code 是业务错误；成功但缺少必需 data 是解析错误。允许无数据的接口调用 checkSuccess(code)，不要对它调用 requireData。message 可读取，但框架不把服务器 message 自动展示给用户，UI 使用本地化文案。

Gson 字段映射 code/message/data 使用 SerializedName，R8 规则保留模型字段。接口字段名称或成功规则不同，应使用业务 DTO 和 Repository 适配，不修改框架以猜测所有服务端协议。

## Kotlin ViewModel

```kotlin
private val request = BaseRequest<List<Item>>(viewModelScope)
val requestState: LiveData<RequestState<List<Item>>> = request.state

fun refresh() {
    request.launch { api.items() }
    // Repository 已返回 ApiResult 时：request.launchResult { repository.items() }
}
```

BaseRequest 的状态为 IDLE、RUNNING、SUCCESS、ERROR、CANCELLED。页面观察 state 并决定全屏 Loading 或保留 Content，框架不会把请求 RUNNING 自动变成页面 LOADING。Sample DemoViewModel 已使用 BaseRequest 管理请求与 requestBusy。

所有发起/主动取消在 Main 操作，耗时阻塞工作由 Repository withContext(IO) 执行。scope 由调用方提供；ViewModel 使用 viewModelScope，页面请求使用 LifecycleOwner 的 lifecycleScope。每个实例是一个独立请求通道，新请求取消同实例旧任务；并行独立任务创建不同实例。同实例旧任务不能覆盖新任务状态。协程取消传播为 CancellationException；LiveData 可观察 CANCELLED，但取消不会变成 AppError。

## Java UI

```java
BaseRequest<List<Photo>> request = BaseRequest.forOwner(getViewLifecycleOwner());
request.getState().observe(getViewLifecycleOwner(), state -> {
    switch (state.getStatus()) {
        case RUNNING: /* update request indicator */ break;
        case SUCCESS: adapter.submitList(state.getData()); break;
        case ERROR: /* map state.getError() to strings.xml */ break;
        case CANCELLED: /* no business error */ break;
        default: break;
    }
});
CancelHandle handle = request.enqueue(api.photosCall(12));
// handle.cancel(); 或 request.cancel();
```

对响应封装接口：`request.enqueueResponse(api.profileCall(), 200)`。每次发起创建新的 Retrofit Call，不复用已执行 Call。Fragment 必须传 getViewLifecycleOwner；owner 销毁会取消 Call。Java 不调用 launch/launchResult（这些是 Kotlin suspend lambda 入口），只使用普通 enqueue/enqueueResponse。

## 不变的边界

Token 刷新、幂等重试、HTTP 超时仍由 NetworkClient 管理；UI 页面内容与业务缓存由 ViewModel/Repository 管理。BaseRequest 不持有 Activity/Fragment/Binding，且不负责多个通道的全局 Loading 计数。
