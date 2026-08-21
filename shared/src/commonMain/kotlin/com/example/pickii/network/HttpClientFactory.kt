package com.example.pickii.network

// 스파이크용 임시 제거, 버전 정책 결정 후 복구 (ktor 3.5.2 iOS klib가
// Kotlin 2.3.21/ABI 2.3.0로 빌드돼 있어 2.2.10 컴파일러가 못 읽음 —
// commonMain의 ktor 의존성을 shared/build.gradle.kts에서 임시로 뺐음)
// import io.ktor.client.HttpClient
// import io.ktor.client.engine.HttpClientEngineFactory
// import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
// import io.ktor.client.plugins.logging.LogLevel
// import io.ktor.client.plugins.logging.Logging
// import io.ktor.serialization.kotlinx.json.json
// import kotlinx.serialization.json.Json
//
// /**
//  * 플랫폼별 Ktor 엔진(Android=OkHttp, iOS=Darwin)을 고르는 진입점. Phase 2에서
//  * [com.example.pickii.data.remote.AuthInterceptor]/`TokenAuthenticator` 로직을 Ktor `Auth` 플러그인으로
//  * 옮길 때 이 팩토리 위에 인증/토큰 갱신을 얹는다. 아직 어떤 API 서비스도 이 클라이언트를 쓰지 않는다.
//  */
// internal expect fun httpClientEngine(): HttpClientEngineFactory<*>
//
// fun createHttpClient(
//     json: Json,
//     enableLogging: Boolean
// ): HttpClient =
//     HttpClient(httpClientEngine()) {
//         install(ContentNegotiation) { json(json) }
//         if (enableLogging) {
//             install(Logging) { level = LogLevel.BODY }
//         }
//     }
