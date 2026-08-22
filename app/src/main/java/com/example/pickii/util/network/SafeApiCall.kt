package com.example.pickii.util.network

import com.example.pickii.data.remote.dto.ApiErrorBody
import com.example.pickii.data.remote.dto.ApiException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

private const val UNKNOWN_ERROR_CODE = "UNKNOWN_ERROR"

/**
 * Retrofit 응답을 공통 성공/에러 포맷에 맞춰 [Result]로 변환한다.
 *
 * 성공(2xx)이면 응답 바디를 그대로 담아 반환하고, 실패면 에러 바디를 [ApiErrorBody]로 파싱해
 * [ApiException]으로 감싼다. 네트워크 예외(IOException)도 동일하게 [Result.failure]로 감싼다.
 *
 * Ktor로 전환된 서비스는 `shared`의 동명 함수(파라미터가 `HttpResponse`인 오버로드)를 쓴다 —
 * 두 함수가 같은 패키지(`com.example.pickii.util.network`)에 공존하고, 호출부 람다가 반환하는
 * 타입(`Response<T>` vs `HttpResponse`)으로 자동 구분된다. Retrofit이 남아있는 동안만 유지.
 */
@Deprecated("Ktor 전환 완료 후 제거. 새 코드는 shared의 safeApiCall(HttpResponse 오버로드)을 쓸 것.")
suspend fun <T> safeApiCall(
    json: Json,
    call: suspend () -> Response<T>
): Result<T> =
    try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(ApiException(UNKNOWN_ERROR_CODE, "응답 본문이 비어 있습니다."))
            }
        } else {
            Result.failure(response.toApiException(json))
        }
    } catch (e: IOException) {
        Result.failure(e)
    }

/** 응답 본문이 없는(204 No Content) 엔드포인트를 위한 변형. 성공이면 [Unit]을 반환한다. */
@Deprecated("Ktor 전환 완료 후 제거. 새 코드는 shared의 safeApiCallUnit(HttpResponse 오버로드)을 쓸 것.")
suspend fun safeApiCallUnit(
    json: Json,
    call: suspend () -> Response<Unit>
): Result<Unit> =
    try {
        val response = call()
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(response.toApiException(json))
        }
    } catch (e: IOException) {
        Result.failure(e)
    }

private fun <T> Response<T>.toApiException(json: Json): ApiException {
    val errorBodyString = errorBody()?.string()
    val parsed = errorBodyString?.let { runCatching { json.decodeFromString<ApiErrorBody>(it) }.getOrNull() }
    return if (parsed != null) {
        ApiException(parsed.error.code, parsed.error.message)
    } else {
        ApiException(UNKNOWN_ERROR_CODE, "요청이 실패했습니다. (HTTP ${code()})")
    }
}
