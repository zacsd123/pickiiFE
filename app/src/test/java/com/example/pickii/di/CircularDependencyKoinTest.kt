package com.example.pickii.di

import org.junit.After
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * `OkHttpClient → TokenAuthenticator → AuthApiService → Retrofit → OkHttpClient` 순환을
 * 실제 클래스가 아니라 같은 모양의 가짜 클래스로 최소 재현해서, Koin이 순환을 어떻게 다루는지
 * 회귀 검사한다 (실제 `TokenAuthenticator`를 대상으로 하지 않는 이유: 네트워크 계층까지 전부
 * Koin으로 옮겨야 실제 그래프로 테스트할 수 있는데, 그건 다음 단계 작업이다. 그때 실제 클래스
 * 대상 테스트를 별도로 추가한다).
 *
 * 이 테스트가 실패하면 둘 중 하나다:
 * - Koin 버전이 올라가면서 순환 의존성을 감지/처리하는 방식이 바뀌었거나
 * - 누군가 `di/InfraModule.kt`의 `TokenAuthenticator` 등록에서 `lazy { get() }`을 걷어내 실제
 *   운영 코드에도 같은 문제가 생겼을 가능성 — 그 경우 이 테스트가 아니라 `di/InfraModule.kt`와
 *   `TokenAuthenticator.kt`의 클래스 주석을 먼저 확인할 것.
 */
class CircularDependencyKoinTest {
    @After
    fun tearDown() {
        stopKoin()
    }

    class FakeOkHttpClient(
        val authenticator: Any
    )

    class FakeRetrofit(
        val client: FakeOkHttpClient
    )

    class FakeAuthApiService(
        val retrofit: FakeRetrofit
    )

    /** eager 버전 — 생성자에서 즉시 [FakeAuthApiService]를 물고 있어 순환이 실제로 발생한다. */
    class FakeTokenAuthenticatorEager(
        val apiService: FakeAuthApiService
    )

    /** lazy 버전 — 이 프로젝트가 실제로 쓰는 패턴. */
    class FakeTokenAuthenticatorLazy(
        val apiServiceLazy: Lazy<FakeAuthApiService>
    )

    @Test
    fun `순환을 즉시 get()으로 풀면 StackOverflowError가 난다`() {
        val cyclicModule =
            module {
                single { FakeTokenAuthenticatorEager(get()) }
                single { FakeOkHttpClient(authenticator = get<FakeTokenAuthenticatorEager>()) }
                single { FakeRetrofit(get()) }
                single { FakeAuthApiService(get()) }
            }
        val koin = startKoin { modules(cyclicModule) }.koin

        assertFailsWith<StackOverflowError> { koin.get<FakeOkHttpClient>() }
    }

    @Test
    fun `lazy 로 감싸면 순환 없이 resolve되고 동일 싱글턴을 되돌려준다`() {
        val lazyModule =
            module {
                single { FakeTokenAuthenticatorLazy(apiServiceLazy = lazy { get<FakeAuthApiService>() }) }
                single { FakeOkHttpClient(authenticator = get<FakeTokenAuthenticatorLazy>()) }
                single { FakeRetrofit(get()) }
                single { FakeAuthApiService(get()) }
            }
        val koin = startKoin { modules(lazyModule) }.koin

        val client = koin.get<FakeOkHttpClient>()
        val authenticator = client.authenticator as FakeTokenAuthenticatorLazy

        // Lazy.value를 여기서 처음 건드린다 — 실제 코드의 "401이 나서 갱신하는 시점"과 대응.
        val apiService = authenticator.apiServiceLazy.value

        assertEquals(client, apiService.retrofit.client, "Retrofit을 거쳐 되돌아온 OkHttpClient가 같은 싱글턴이어야 함")
    }
}
