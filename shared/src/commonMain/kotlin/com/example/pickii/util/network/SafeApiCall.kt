package com.example.pickii.util.network

import com.example.pickii.data.remote.dto.ApiErrorBody
import com.example.pickii.data.remote.dto.ApiException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.io.IOException

private const val UNKNOWN_ERROR_CODE = "UNKNOWN_ERROR"

/**
 * Ktor 응답을 공통 성공/에러 포맷에 맞춰 [Result]로 변환한다.
 *
 * 성공(2xx)이면 응답 바디를 [T]로 역직렬화해서 반환하고(`ContentNegotiation` 플러그인이 처리),
 * 실패면 에러 바디를 [ApiErrorBody]로 파싱해 [ApiException]으로 감싼다. 네트워크 예외(IOException)도
 * 동일하게 [Result.failure]로 감싼다.
 */
suspend inline fun <reified T> safeApiCall(call: suspend () -> HttpResponse): Result<T> =
    try {
        val response = call()
        if (response.status.isSuccess()) {
            Result.success(response.body<T>())
        } else {
            Result.failure(response.toApiException())
        }
    } catch (e: IOException) {
        Result.failure(e)
    }

/** 응답 본문이 없는(204 No Content) 엔드포인트를 위한 변형. 성공이면 [Unit]을 반환한다. */
suspend fun safeApiCallUnit(call: suspend () -> HttpResponse): Result<Unit> =
    try {
        val response = call()
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(response.toApiException())
        }
    } catch (e: IOException) {
        Result.failure(e)
    }

/** 문자열 id를 [Long]으로 변환하지 못했을 때 던질 예외. */
fun invalidIdException(id: String) = IllegalArgumentException("잘못된 id: $id")

@PublishedApi
internal suspend fun HttpResponse.toApiException(): ApiException {
    val parsed = runCatching { body<ApiErrorBody>() }.getOrNull()
    return if (parsed != null) {
        ApiException(parsed.error.code, parsed.error.message)
    } else {
        ApiException(UNKNOWN_ERROR_CODE, "요청이 실패했습니다. (HTTP ${status.value})")
    }
}
