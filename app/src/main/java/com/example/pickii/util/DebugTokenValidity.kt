package com.example.pickii.util

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class JwtExpPayload(
    val exp: Long? = null
)

private val jwtExpPayloadJson = Json { ignoreUnknownKeys = true }

/**
 * TODO(디버그 검증용 임시 코드, 확인 끝나면 삭제): 액세스 토큰의 `exp` 클레임에서 만료까지 남은
 * 시간(초)을 계산한다. 토큰 값 자체는 이 함수도, 호출하는 쪽도 절대 로그로 남기지 않을 것 —
 * 여기서는 결과값(초)만 반환한다.
 *
 * `PickiiApplication`(Android 전용)에서만 쓰여서 `util/JwtDecoder.kt`(shared/commonMain,
 * `decodeJwtSubject`)와 별도로 app에 남겨뒀다.
 */
fun debugRemainingValiditySeconds(token: String): Long? {
    val segments = token.split(".")
    if (segments.size != 3) return null

    return runCatching {
        val payloadBytes = Base64.decode(segments[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val exp = jwtExpPayloadJson.decodeFromString<JwtExpPayload>(String(payloadBytes)).exp ?: return null
        exp - System.currentTimeMillis() / 1000
    }.getOrNull()
}
