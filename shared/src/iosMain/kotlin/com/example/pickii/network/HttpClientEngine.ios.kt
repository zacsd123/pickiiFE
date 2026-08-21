package com.example.pickii.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun httpClientEngine(): HttpClientEngineFactory<*> = Darwin
