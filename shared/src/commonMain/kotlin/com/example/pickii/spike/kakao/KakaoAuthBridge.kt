package com.example.pickii.spike.kakao

/**
 * 카카오 로그인의 실제 구현을 플랫폼 쪽에서 주입받기 위한 인터페이스.
 *
 * 카카오는 KMP 공식 아티팩트가 없다. Android는 Kakao Android SDK를 commonMain에서 바로 쓸 수
 * 없어 androidMain에서, iOS는 Kakao iOS SDK가 순수 Swift·SPM 전용 배포라 Kotlin/Native cinterop이
 * 안 통해서 iosApp의 Swift 코드에서 각각 구현체를 만들어 [KakaoAuthBridgeHolder]에 주입한다.
 *
 * suspend가 아니라 콜백 형태로 둔 이유: Kotlin의 suspend 함수를 Swift 쪽에서 구현하게 하려면
 * Kotlin/Native의 Swift async 상호운용(실험적)에 의존해야 해서, 스파이크 범위에서는 가장 단순한
 * 콜백 시그니처로 언어 경계를 넘긴다.
 */
interface KakaoAuthBridge {
    /** 카카오 로그인을 시도하고 결과를 콜백으로 돌려준다. 성공 시 accessToken, 실패 시 errorMessage. */
    fun login(onResult: (accessToken: String?, errorMessage: String?) -> Unit)
}

/**
 * [KakaoAuthBridge] 구현체를 담아두는 자리.
 *
 * TODO(Koin): 지금은 스파이크라 전역 var로 두지만, Hilt→Koin 전환이 끝나면 Koin 모듈에서
 * 플랫폼별 [KakaoAuthBridge] 구현체를 바인딩하고 ViewModel에 생성자 주입하는 방식으로 바꿔야 한다.
 * (androidMain 쪽 구현체 + iosApp Swift가 채워주는 구현체를 각각 Koin 모듈에 등록)
 */
object KakaoAuthBridgeHolder {
    var bridge: KakaoAuthBridge? = null
}
