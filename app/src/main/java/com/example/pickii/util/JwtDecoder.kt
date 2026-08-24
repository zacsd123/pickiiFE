package com.example.pickii.util

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class JwtPayload(
    val sub: String? = null,
    val exp: Long? = null
)

private val jwtPayloadJson = Json { ignoreUnknownKeys = true }

/**
 * JWT의 payload에서 subject(`sub`) 클레임만 읽는다. 서명 검증은 하지 않는다 — 서버가 발급해서
 * `HttpClientFactory`의 Ktor `Auth` 플러그인이 그대로 얹어 보내는 토큰을 읽기만 하는
 * 용도라 위변조 여부는 문제되지 않는다.
 *
 * 소셜 로그인(1-10) 응답에는 memberId가 내려오지 않는다. 정상 로그인과 같은 인증 서버가 발급한
 * 토큰이므로, Spring 계열 백엔드의 일반적인 관례대로 subject 클레임에 회원 id가 들어있다고 보고
 * 이를 회원 id의 대체 출처로 쓴다. `sub`가 없거나 숫자가 아니면 null을 반환한다.
 */
fun decodeJwtSubject(token: String): String? {
    val segments = token.split(".")
    if (segments.size != 3) return null

    return runCatching {
        val payloadBytes = Base64.decode(segments[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        jwtPayloadJson.decodeFromString<JwtPayload>(String(payloadBytes)).sub
    }.getOrNull()
}

/**
 * TODO(디버그 검증용 임시 코드, 확인 끝나면 삭제): 액세스 토큰의 `exp` 클레임에서 만료까지 남은
 * 시간(초)을 계산한다. 토큰 값 자체는 이 함수도, 호출하는 쪽도 절대 로그로 남기지 않을 것 —
 * 여기서는 결과값(초)만 반환한다.
 */
fun debugRemainingValiditySeconds(token: String): Long? {
    val segments = token.split(".")
    if (segments.size != 3) return null

    return runCatching {
        val payloadBytes = Base64.decode(segments[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val exp = jwtPayloadJson.decodeFromString<JwtPayload>(String(payloadBytes)).exp ?: return null
        exp - System.currentTimeMillis() / 1000
    }.getOrNull()
}
