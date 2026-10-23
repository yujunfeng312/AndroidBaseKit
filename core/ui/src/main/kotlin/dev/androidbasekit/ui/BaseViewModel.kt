package dev.androidbasekit.ui

import androidx.lifecycle.*

/**
 * 轻量页面 ViewModel 基类。提供 pageState 和 requestBusy 两组 LiveData，不持有 Activity、Fragment 或 View。
 */
open class BaseViewModel : ViewModel() {
    private val page = MutableLiveData(PageState.loading())
    private val busy = MutableLiveData(false)
    val pageState: LiveData<PageState> = page
    val requestBusy: LiveData<Boolean> = busy
    protected fun setPageState(state: PageState) { page.value = state }
    protected fun setRequestBusy(value: Boolean) { busy.value = value }
}
