package com.example.pickii.util.kakao

import androidx.compose.runtime.Composable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 카카오 로그인의 실제 구현을 플랫폼 쪽에서 주입받기 위한 인터페이스.
 *
 * 카카오는 KMP 공식 아티팩트가 없다. Android는 Kakao Android SDK를 commonMain에서 바로 쓸 수
 * 없어 androidMain에서, iOS는 Kakao iOS SDK가 순수 Swift·SPM 전용 배포라 Kotlin/Native cinterop이
 * 안 통해서 iosApp의 Swift 코드에서 각각 구현체를 만들어 [rememberKakaoAuthBridge]로 얻는다.
 *
 * suspend가 아니라 콜백 형태로 둔 이유: Kotlin의 suspend 함수를 Swift 쪽에서 구현하게 하려면
 * Kotlin/Native의 Swift async 상호운용(실험적)에 의존해야 해서, 가장 단순한 콜백 시그니처로
 * 언어 경계를 넘긴다. commonMain 호출부는 [loginSuspending]/[getUserIdSuspending] 확장 함수로
 * 평소처럼 suspend로 쓴다.
 */
interface KakaoAuthBridge {
    /** 카카오 로그인을 시도하고 결과를 콜백으로 돌려준다. 성공 시 accessToken, 실패 시 errorMessage. */
    fun login(onResult: (accessToken: String?, errorMessage: String?) -> Unit)

    /** 로그인된 카카오 사용자의 고유 id(providerId로 사용)를 콜백으로 돌려준다. */
    fun getUserId(onResult: (userId: Long?, errorMessage: String?) -> Unit)
}

/**
 * [KakaoAuthBridge] 구현체를 컴포지션에서 얻는다.
 *
 * Android 카카오 SDK는 카카오톡 앱 전환을 위해 Activity Context가 필요해서, actual 구현이
 * `LocalContext.current`로 얻은 Context를 그대로 구현체에 넣어준다(안드로이드는 매 컴포지션마다
 * 새로 만들어 쓰고 별도 저장소가 없다). iOS actual은 앱 시작 시점([KakaoAuthBridgeHolder]에)
 * Swift가 미리 넣어둔 구현체를 그대로 반환한다 — Swift가 Compose 트리보다 먼저 초기화되기
 * 때문에 이 지점에서는 주입받은 값을 읽기만 한다.
 */
@Composable
expect fun rememberKakaoAuthBridge(): KakaoAuthBridge

/**
 * [KakaoAuthBridge] 구현체를 담아두는 자리.
 *
 * iOS만 쓴다 — Swift가 Compose 트리 생성 전(`iOSApp.swift`의 `init()`)에 구현체를 만들어 여기 넣어두면
 * [rememberKakaoAuthBridge]의 iOS actual이 그 값을 그대로 돌려준다. Android는 [rememberKakaoAuthBridge]
 * 안에서 `LocalContext.current`로 그때그때 만들기 때문에 이 객체를 쓰지 않는다.
 *
 * TODO(Koin, iOS 전용): Swift 쪽 주입을 이 전역 var 대신 Koin 컨테이너에 등록하는 방식으로 바꾸는 작업이
 * 남아 있다 — iosApp.swift 변경과 iOS 컴파일 검증이 같이 필요해서 맥에서 진행한다
 * (PROGRESS_kmp-migration.md 참고).
 */
object KakaoAuthBridgeHolder {
    var bridge: KakaoAuthBridge? = null
}

/** [KakaoAuthBridge.login]을 suspend로 감싼 확장 함수. 호출부는 콜백 대신 이걸 쓴다. */
suspend fun KakaoAuthBridge.loginSuspending(): Result<String> =
    suspendCancellableCoroutine { cont ->
        login { accessToken, errorMessage ->
            cont.resume(
                if (accessToken != null) Result.success(accessToken)
                else Result.failure(IllegalStateException(errorMessage ?: "카카오 로그인 실패"))
            )
        }
    }

/** [KakaoAuthBridge.getUserId]를 suspend로 감싼 확장 함수. 호출부는 콜백 대신 이걸 쓴다. */
suspend fun KakaoAuthBridge.getUserIdSuspending(): Result<Long> =
    suspendCancellableCoroutine { cont ->
        getUserId { userId, errorMessage ->
            cont.resume(
                if (userId != null) Result.success(userId)
                else Result.failure(IllegalStateException(errorMessage ?: "카카오 사용자 정보 조회 실패"))
            )
        }
    }
