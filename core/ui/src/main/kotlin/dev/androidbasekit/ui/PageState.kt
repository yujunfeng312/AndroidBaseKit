package dev.androidbasekit.ui

import androidx.annotation.StringRes

/**
 * 页面展示状态。加载、内容、空数据、错误；刷新已有内容时可保留 CONTENT 并单独显示请求忙碌。
 */
enum class PageStatus { LOADING, CONTENT, EMPTY, ERROR }
/**
 * 不可变的页面状态模型。保存展示状态及可选字符串资源 ID，提供 Java/Kotlin 工厂方法。
 */
data class PageState @JvmOverloads constructor(val status: PageStatus, @param:StringRes val messageRes: Int = 0) {
    companion object {
        @JvmStatic fun loading() = PageState(PageStatus.LOADING)
        @JvmStatic fun content() = PageState(PageStatus.CONTENT)
        @JvmStatic fun empty() = PageState(PageStatus.EMPTY)
        @JvmStatic fun error(@StringRes message: Int) = PageState(PageStatus.ERROR, message)
    }
}
