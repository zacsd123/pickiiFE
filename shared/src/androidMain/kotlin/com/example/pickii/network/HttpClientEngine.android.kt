package com.example.pickii.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun httpClientEngine(): HttpClientEngineFactory<*> = OkHttp
