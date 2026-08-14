package com.example.pickii.data.remote

import com.example.pickii.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** 저장된 Access Token이 있으면 모든 요청에 `Authorization: Bearer {token}` 헤더를 붙인다. */
class AuthInterceptor
    @Inject
    constructor(
        private val tokenStore: TokenStore
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val accessToken = tokenStore.currentAccessTokenBlocking()
            val request = chain.request()
            return if (accessToken.isNullOrBlank()) {
                chain.proceed(request)
            } else {
                chain.proceed(
                    request
                        .newBuilder()
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()
                )
            }
        }
    }
