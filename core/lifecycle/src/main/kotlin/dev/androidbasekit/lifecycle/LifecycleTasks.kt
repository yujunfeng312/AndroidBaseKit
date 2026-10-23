package dev.androidbasekit.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

/**
 * 生命周期后台任务的取消句柄。取消是协作式的，Runnable 应响应中断。
 */
fun interface TaskHandle { fun cancel() }
/**
 * Java 生命周期后台任务入口。在 IO 调度器运行 Runnable，LifecycleOwner 销毁时取消；Kotlin 挂起任务可直接用 lifecycleScope。
 */
object LifecycleTasks {
    /** Runnable must cooperate with interruption/cancellation; suspending Kotlin tasks should use lifecycleScope directly. */
    @JvmStatic fun launchIo(owner: LifecycleOwner, action: Runnable): TaskHandle {
        check(owner.lifecycle.currentState != Lifecycle.State.DESTROYED)
        val job = owner.lifecycleScope.launch(Dispatchers.IO) { runInterruptible { action.run() } }
        return TaskHandle { job.cancel() }
    }
}
