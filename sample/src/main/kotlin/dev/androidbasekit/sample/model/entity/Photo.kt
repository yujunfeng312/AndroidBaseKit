package dev.androidbasekit.sample.model.entity

/**
 * JSONPlaceholder 图片响应 DTO。保存 id、标题和缩略图地址，映射为本地 DemoRecord 后展示。
 */
data class Photo(val id: Long, val title: String, val thumbnailUrl: String)
