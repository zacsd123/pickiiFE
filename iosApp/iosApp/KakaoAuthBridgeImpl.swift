import KakaoSDKAuth
import KakaoSDKUser
import Shared

/// `com.example.pickii.util.kakao.KakaoAuthBridge`(Kotlin)의 iOS 구현.
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

    // TODO(iOS 검증 필요, 윈도우 환경이라 미확인): KotlinLong? 시그니처가 실제 생성된 Shared
    // 헤더와 맞는지, me() 실패 시 error가 nil인데 user.id도 nil인 케이스가 실제로 있는지 확인.
    func getUserId(onResult: @escaping (KotlinLong?, String?) -> Void) {
        UserApi.shared.me { user, error in
            if let error = error {
                onResult(nil, error.localizedDescription)
            } else if let id = user?.id {
                onResult(KotlinLong(value: id), nil)
            } else {
                onResult(nil, "카카오 사용자 정보 조회 실패")
            }
        }
    }
}
