package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.TokenRefreshRequest
import com.example.pickii.data.remote.dto.TokenRefreshResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * `TokenAuthenticator`(OkHttp `Authenticator`, Retrofit 기반 서비스 12개가 아직 쓰는
 * OkHttpClient에 붙어있음)가 401 갱신에 쓰는 전용 Retrofit 인터페이스.
 *
 * [AuthApiService]가 Ktor로 전환되면서 더 이상 Retrofit `Response<T>`를 반환하지 않기 때문에,
 * 같은 `auth/token/refresh` 엔드포인트를 부르는 Retrofit 전용 경로를 따로 둔다. Retrofit이
 * 완전히 제거되면(90개 엔드포인트 전환 끝나고) 이 인터페이스와 `TokenAuthenticator`도 함께 삭제된다.
 */
interface RetrofitAuthRefreshService {
    @POST("auth/token/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequest
    ): Response<ApiEnvelope<TokenRefreshResponseDto>>
}
