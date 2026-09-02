package com.example.pickii.di

import androidx.lifecycle.SavedStateHandle
import com.example.pickii.ui.navigation.ARG_MEMBER_ID
import com.example.pickii.ui.navigation.ARG_POST_ID
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * `KoinGraphResolveTest`(androidHostTest)의 iOS 대응판. Android 쪽 그래프만 확인하고 iOS
 * 그래프는 아무도 안 본 게 리포지토리 레이어 DI 갭(2026-08-26)을 놓친 원인이었다 — `initKoin()`이
 * 실제로 Android의 `startKoin`과 같은 화면들을 resolve할 수 있는지 iOS 타깃에서 직접 실행해서
 * 확인한다.
 *
 * **`setUp()`이 부르는 `initKoin()`은 `iOSApp.swift`의 `init()`이 부르는 것과 완전히 같은
 * 함수다**(`InitKoinKt.doInitKoin()`) — 테스트가 손으로 모듈 목록을 따로 조립하지 않는다.
 * 그래서 "앱이 실제로 시작하는 그래프"와 "이 테스트가 검사하는 그래프"가 정의상 같다 — `initKoin()`
 * 자체에서 모듈 하나가 빠지면(예: `sharedCalendarRepositoryModule`) 그 즉시 이 테스트가 해당
 * 모듈에 의존하는 정의에서 실패한다(실측 확인, 2026-09-03).
 *
 * 네트워크는 필요 없다 — `HttpClient`/리포지토리 인스턴스를 "만들 수 있는가"만 보는 테스트라
 * 백엔드가 꺼져 있어도 통과해야 정상이다(실제 API 호출은 `viewModelScope.launch { }` 안에서
 * 일어나는데 여기서는 그 코루틴을 실행시키지 않는다 — Android `KoinGraphResolveTest`와 동일 원리).
 *
 * **손목록이 아니라 등록된 정의를 전부 순회해서 resolve한다(2026-09-03).** 원래는
 * `koin.get<...ViewModel>()`을 화면 하나씩 손으로 나열했는데, `LoginViewModel`이
 * `sharedModule`엔 등록됐지만 이 목록엔 추가가 안 됐던 걸 놓친 적이 있다(다른 환경의 Kakao
 * 실연동 작업, 2026-09-01 — `chat/list` 이식 중 발견). Koin의 `checkModules`/`verify()`는
 * JVM 리플렉션 기반이라 iOS(Kotlin/Native)에서 못 쓴다(`koin-test-jvm`에만 있고 진짜
 * 멀티플랫폼인 `koin-test`엔 없음, 실측 확인). 대신 `Koin.instanceRegistry`(공개 API지만
 * `@KoinInternalApi` opt-in 필요)를 순회해서 등록된 `BeanDefinition`마다 `primaryType`
 * (`KClass<*>`, 리플렉션 아님 — 정의 시점에 이미 알고 있는 타입)로 `Koin.get(KClass<*>, ...)`을
 * 직접 호출한다. 이러면 "등록은 됐는데 이 테스트가 모른다"는 부류의 사고 자체가 구조적으로
 * 불가능해진다 — `sharedModule`에 새 바인딩을 추가하면 이 테스트가 자동으로 같이 커버한다.
 *
 * **한계 1**: 이 테스트는 "등록된 것이 resolve되는가"만 본다 — "resolve돼야 할 게 애초에
 * 등록조차 안 됐는가"는 여전히 못 잡는다(이건 기존 손목록 방식도 마찬가지였다 — 등록 안 된
 * 타입은 애초에 `get<T>()` 줄을 쓸 방법이 없었다). 이 구멍은 테스트가 아니라 화면 이식 때마다
 * 옮긴 화면을 실제로 카나리아로 띄워보는 단계가 메운다(KMP_MIGRATION_PLAN.md §5 14번 참고) —
 * 등록이 없으면 그 화면에 진입하는 순간 `NoDefinitionFoundException`으로 죽는다.
 *
 * **한계 2**: `koin.get<Any>(type, qualifier = null, parameters = null)`로 파라미터 없이
 * 호출한다 — 지금은 `sharedModule`에 `parametersOf`를 쓰는 정의가 하나도 없어서 전부 통과하지만,
 * 나중에 파라미터 있는 정의가 추가되면 이 순회에서 진짜 결함이 아닌데도 실패한다. 그때는 그
 * 타입을 순회에서 제외하거나 더미 파라미터를 넘기도록 손봐야 한다.
 */
class IosKoinGraphResolveTest {
    @BeforeTest
    fun setUp() {
        initKoin()
        // SavedStateHandle을 받는 ViewModel(MemberProfileViewModel 등)은 Android koinViewModel()이
        // Compose Navigation 스코프에서 자동으로 채워주는 값이라, 여기서는 테스트 전용으로 직접
        // 등록한다 — Android KoinGraphResolveTest와 동일한 이유(KoinGraphResolveTest.kt 참고).
        loadKoinModules(
            module {
                single {
                    SavedStateHandle(
                        mapOf(ARG_MEMBER_ID to "test-member-id", ARG_POST_ID to "test-post-id")
                    )
                }
            }
        )
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @OptIn(KoinInternalApi::class)
    @Test
    fun `shared 모듈에 등록된 정의가 전부 iOS Koin 그래프에서 resolve된다`() {
        val koin: Koin = KoinPlatformTools.defaultContext().get()
        val registeredTypes = koin.instanceRegistry.instances.values.map { it.beanDefinition.primaryType }.distinct()

        registeredTypes.forEach { type ->
            koin.get<Any>(type, qualifier = null, parameters = null)
        }
    }
}
