package dev.androidbasekit.ui

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt

/** 返回独立 Spannable，保留已有 span；不修改调用方文本，不持有 View。 */
object TextSpans {
    /** start/end 为 UTF-16 索引，区间左闭右开；非法边界抛异常。 */
    @JvmStatic fun colorRange(text: CharSequence, start: Int, end: Int, @ColorInt color: Int): SpannableString {
        require(start >= 0 && end >= start && end <= text.length) { "Invalid text range" }
        return SpannableString(text).apply {
            if (start < end) setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /** 对所有不重叠、区分大小写的匹配着色；空关键词不处理。 */
    @JvmStatic fun colorMatches(text: CharSequence, keyword: String, @ColorInt color: Int): SpannableString {
        val result = SpannableString(text)
        if (keyword.isEmpty()) return result
        val source = text.toString()
        var index = source.indexOf(keyword)
        while (index >= 0) {
            result.setSpan(ForegroundColorSpan(color), index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            index = source.indexOf(keyword, index + keyword.length)
        }
        return result
    }
}
