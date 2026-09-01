package com.example.pickii.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
private data class JwtPayload(
    val sub: String? = null
)

private val jwtPayloadJson = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalEncodingApi::class)
private val jwtBase64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)

/**
 * JWT의 payload에서 subject(`sub`) 클레임만 읽는다. 서명 검증은 하지 않는다 — 서버가 발급해서
 * `HttpClientFactory`의 Ktor `Auth` 플러그인이 그대로 얹어 보내는 토큰을 읽기만 하는
 * 용도라 위변조 여부는 문제되지 않는다.
 *
 * 소셜 로그인(1-10) 응답에는 memberId가 내려오지 않는다. 정상 로그인과 같은 인증 서버가 발급한
 * 토큰이므로, Spring 계열 백엔드의 일반적인 관례대로 subject 클레임에 회원 id가 들어있다고 보고
 * 이를 회원 id의 대체 출처로 쓴다. `sub`가 없거나 숫자가 아니면 null을 반환한다.
 */
@OptIn(ExperimentalEncodingApi::class)
fun decodeJwtSubject(token: String): String? {
    val segments = token.split(".")
    if (segments.size != 3) return null

    return runCatching {
        val payloadBytes = jwtBase64.decode(segments[1])
        jwtPayloadJson.decodeFromString<JwtPayload>(payloadBytes.decodeToString()).sub
    }.getOrNull()
}
