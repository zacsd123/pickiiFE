package com.example.pickii.util

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class JwtPayload(
    val sub: String? = null
)

private val jwtPayloadJson = Json { ignoreUnknownKeys = true }

/**
 * JWT의 payload에서 subject(`sub`) 클레임만 읽는다. 서명 검증은 하지 않는다 — 서버가 발급해서
 * [com.example.pickii.data.remote.AuthInterceptor]가 그대로 얹어 보내는 토큰을 읽기만 하는
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
