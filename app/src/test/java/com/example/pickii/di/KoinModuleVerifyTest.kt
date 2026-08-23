package com.example.pickii.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Koin 4.1.1의 정적 모듈 검증. `KoinGraphResolveTest`(실제 인스턴스 생성)와는 성격이 다르다 —
 * 이건 리플렉션으로 "이 타입을 만들 수 있는 정의가 모듈 안에 있는가"만 확인하고 실제로 객체를
 * 만들어보지는 않는다(그래서 순환 의존성 같은 런타임 문제는 못 잡는다 — `CircularDependencyKoinTest`,
 * `KoinGraphResolveTest` 참고). 대신 "바인딩 자체를 빼먹었다"는 실수는 이 테스트가 더 빨리, 더
 * 정확한 위치로 잡아준다.
 *
 * `Context`/`SavedStateHandle`은 실제 클래스가 아니라 이 프로젝트 밖에서 주입되는 값이라
 * `extraTypes`로 "존재한다고 가정하라"고 알려준다. `HttpClientEngine`도 마찬가지 이유로 추가했다 —
 * `networkModule`의 `single<HttpClient>`가 `OkHttp.create()`로 엔진을 직접 만들어 넘기지 Koin에
 * 요청하지 않는데, `verify()`가 `HttpClient`의 실제 생성자(`engine: HttpClientEngine`)까지
 * 리플렉션으로 따라 들어가서 오탐(false positive)한다.
 */
class KoinModuleVerifyTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `모든 모듈의 바인딩이 다 있다`() {
        module {
            includes(infraModule, networkModule, repositoryModule, calendarRepositoryModule, viewModelModule)
        }.verify(extraTypes = listOf(Context::class, SavedStateHandle::class, HttpClientEngine::class))
    }
}
