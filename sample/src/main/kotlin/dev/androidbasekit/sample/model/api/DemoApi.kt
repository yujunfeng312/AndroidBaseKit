package dev.androidbasekit.sample.model.api

import dev.androidbasekit.sample.model.entity.Photo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit 示例接口。演示 suspend photos 和 Java Call photosCall 两种调用形式。
 */
interface DemoApi {
    @GET("photos") suspend fun photos(@Query("_limit") limit: Int): List<Photo>
    @GET("photos") fun photosCall(@Query("_limit") limit: Int): Call<List<Photo>>
}
