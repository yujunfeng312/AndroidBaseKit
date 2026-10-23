package dev.androidbasekit.network

import com.google.gson.Gson
import dev.androidbasekit.common.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * BaseResponseTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
class BaseResponseTest {
    private fun decode(json: String): BaseResponse<String> {
        val type = object : com.google.gson.reflect.TypeToken<BaseResponse<String>>() {}.type
        return Gson().fromJson(json, type)
    }
    @Test fun defaultSuccessCodeUnwraps() { assertEquals("ok", decode("""{"code":0,"data":"ok"}""").requireData()) }
    @Test fun explicitSuccessCodeUnwraps() { assertEquals("ok", decode("""{"code":200,"data":"ok"}""").requireData(200)) }
    @Test fun businessErrorOnlyExposesCode() = runBlocking {
        val result = ApiErrorMapper.execute { decode("""{"code":403,"message":"private token","data":null}""").requireData() }
        assertEquals(AppError(ErrorKind.BUSINESS, "403"), result.errorOrNull())
    }
    @Test fun missingCodeIsParseError() = runBlocking {
        val result = ApiErrorMapper.execute { decode("""{"data":"ok"}""").requireData() }
        assertEquals(ErrorKind.PARSE, result.errorOrNull()?.kind)
    }
    @Test fun missingRequiredDataIsParseError() = runBlocking {
        val result = ApiErrorMapper.execute { decode("""{"code":0}""").requireData() }
        assertEquals(ErrorKind.PARSE, result.errorOrNull()?.kind)
    }
    @Test fun successfulNoDataEndpointCanCheckCode() { decode("""{"code":0}""").checkSuccess() }
}
