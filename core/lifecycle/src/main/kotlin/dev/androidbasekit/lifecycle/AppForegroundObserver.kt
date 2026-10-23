package dev.androidbasekit.lifecycle

import androidx.lifecycle.*

/**
 * Application-owned observer; close when its owning application/service scope ends. Contains no UI references.
 *
 * 应用进程前后台观察器。通过 LiveData 提供 ProcessLifecycleOwner 状态，无页面引用；由持有方 close 解除观察。
 */
class AppForegroundObserver : DefaultLifecycleObserver, AutoCloseable {
    private val state = MutableLiveData(false)
    val foreground: LiveData<Boolean> = state
    init { ProcessLifecycleOwner.get().lifecycle.addObserver(this) }
    override fun onStart(owner: LifecycleOwner) { state.value = true }
    override fun onStop(owner: LifecycleOwner) { state.value = false }
    override fun close() { ProcessLifecycleOwner.get().lifecycle.removeObserver(this) }
}
