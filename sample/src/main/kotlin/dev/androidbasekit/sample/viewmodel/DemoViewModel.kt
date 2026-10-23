package dev.androidbasekit.sample.viewmodel

import dev.androidbasekit.sample.model.repository.DemoRepository
import dev.androidbasekit.sample.R

import android.app.Application
import androidx.lifecycle.*
import dev.androidbasekit.common.*
import dev.androidbasekit.storage.DemoRecord
import dev.androidbasekit.ui.*
import dev.androidbasekit.network.BaseRequest
import dev.androidbasekit.network.RequestStatus
import kotlinx.coroutines.*

/**
 * 示例页面状态持有者。协调 BaseRequest、Repository、列表和错误状态；销毁后等待数据库任务完成再关闭仓库。
 */
class DemoViewModel(application: Application) : BaseViewModel() {
    private val workScope = viewModelScope
    private val repository = DemoRepository(application)
    private val data = MutableLiveData<List<DemoRecord>>(emptyList())
    val items: LiveData<List<DemoRecord>> = data
    private val failure = MutableLiveData<Int>()
    val refreshError: LiveData<Int> = failure
    private val networkSource = MutableLiveData(false)
    val networkRequest: LiveData<Boolean> = networkSource
    private val request = BaseRequest<List<DemoRecord>>(workScope)
    private val requestObserver = Observer<dev.androidbasekit.network.RequestState<List<DemoRecord>>> { state ->
        setRequestBusy(state.status == RequestStatus.RUNNING)
        if (state.status == RequestStatus.ERROR) onFailure(!data.value.isNullOrEmpty())
    }
    init { request.state.observeForever(requestObserver); loadLocal() }
    fun loadLocal() = load(false)
    fun refreshNetwork() = load(true)
    private fun load(network: Boolean) {
        networkSource.value = network
        request.cancel()
        val hasContent = !data.value.isNullOrEmpty()
        if (!hasContent) setPageState(PageState.loading())
        request.launchResult {
            val result = withContext(Dispatchers.IO) {
                if (network) repository.remoteItems() else {
                    delay(300); ApiResult.success(repository.localItems())
                }
            }
            when (result) {
                is ApiResult.Success -> {
                    data.value = result.data
                    setPageState(if (result.data.isEmpty()) PageState.empty() else PageState.content())
                }
                is ApiResult.Failure -> Unit
            }
            result
        }
    }

    private fun onFailure(hasContent: Boolean) {
        if (hasContent) { setPageState(PageState.content()); failure.value = R.string.refresh_failed }
        else setPageState(PageState.error(R.string.refresh_failed))
    }
    fun showEmpty() { request.cancel(); data.value = emptyList(); setRequestBusy(false); setPageState(PageState.empty()) }
    fun showError() { request.cancel(); data.value = emptyList(); setRequestBusy(false); setPageState(PageState.error(R.string.demo_error)) }
    override fun onCleared() {
        // Scope cancellation is non-blocking. Close after any active database operation has completed.
        request.state.removeObserver(requestObserver)
        val ownerJob = workScope.coroutineContext[Job]
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { ownerJob?.join(); repository.close() }
    }
    /**
     * DemoViewModel 创建工厂。向 ViewModel 注入 Application，不把 Activity Context 保存在 ViewModel 中。
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DemoViewModel::class.java))
            @Suppress("UNCHECKED_CAST") return DemoViewModel(application) as T
        }
    }
}
