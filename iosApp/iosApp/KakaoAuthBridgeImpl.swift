import KakaoSDKAuth
import KakaoSDKUser
import Shared

/// `com.example.pickii.spike.kakao.KakaoAuthBridge`(Kotlin)의 iOS 구현.
/// 순수 Swift·SPM 전용으로 배포되는 Kakao iOS SDK는 Kotlin/Native cinterop으로 직접 연결할 수
/// 없어서, iosApp 쪽 Swift 코드가 실제 SDK를 호출하고 그 결과만 Kotlin 인터페이스로 넘긴다.
final class KakaoAuthBridgeImpl: KakaoAuthBridge {
    func login(onResult: @escaping (String?, String?) -> Void) {
        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk { token, error in
                if let error = error {
                    onResult(nil, error.localizedDescription)
                } else {
                    onResult(token?.accessToken, nil)
                }
            }
        } else {
            UserApi.shared.loginWithKakaoAccount { token, error in
                if let error = error {
                    onResult(nil, error.localizedDescription)
                } else {
                    onResult(token?.accessToken, nil)
                }
            }
        }
    }
}
