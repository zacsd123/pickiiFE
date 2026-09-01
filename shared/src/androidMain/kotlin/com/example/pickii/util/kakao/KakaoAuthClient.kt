package com.example.pickii.util.kakao

import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "KakaoAuthClient"

/**
 * 카카오 SDK의 콜백 기반 로그인 API를 코루틴으로 감싼다.
 *
 * 카카오톡 앱이 설치돼 있으면 앱으로, 아니면 카카오계정(웹) 로그인으로 진행한다. 사용자가
 * 카카오톡에서 직접 취소한 경우에는 계정 로그인으로 넘어가지 않고 그대로 실패를 전달한다
 * (의도치 않게 두 번 로그인 창이 뜨는 것을 막기 위함, 카카오 SDK 공식 가이드 권장 패턴).
 */
object KakaoAuthClient {
    /** 카카오 로그인을 진행하고 [OAuthToken]을 반환한다. */
    suspend fun login(context: Context): Result<OAuthToken> =
        suspendCancellableCoroutine { continuation ->
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error != null) Log.e(TAG, "loginWithKakaoTalk failed", error)
                    when {
                        token != null -> continuation.resume(Result.success(token))
                        error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                            continuation.resume(Result.failure(error))
                        else -> loginWithKakaoAccount(context, continuation)
                    }
                }
            } else {
                loginWithKakaoAccount(context, continuation)
            }
        }

    private fun loginWithKakaoAccount(
        context: Context,
        continuation: CancellableContinuation<Result<OAuthToken>>
    ) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) Log.e(TAG, "loginWithKakaoAccount failed", error)
            if (token != null) {
                continuation.resume(Result.success(token))
            } else {
                continuation.resume(Result.failure(error ?: IllegalStateException("카카오 로그인에 실패했습니다.")))
            }
        }
    }

    /** 로그인된 카카오 사용자의 고유 id(providerId로 사용)를 조회한다. */
    suspend fun getUserId(): Result<Long> =
        suspendCancellableCoroutine { continuation ->
            UserApiClient.instance.me { user, error ->
                if (error != null) Log.e(TAG, "me() failed", error)
                val id = user?.id
                if (id != null) {
                    continuation.resume(Result.success(id))
                } else {
                    continuation.resume(Result.failure(error ?: IllegalStateException("카카오 사용자 정보 조회에 실패했습니다.")))
                }
            }
        }
}
