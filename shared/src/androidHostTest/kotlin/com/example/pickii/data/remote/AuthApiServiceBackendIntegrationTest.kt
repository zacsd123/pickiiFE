package com.example.pickii.data.remote

import com.example.pickii.data.remote.api.KtorAuthApiService
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.LoginRequest
import com.example.pickii.data.remote.dto.LoginResponseDto
import com.example.pickii.util.network.safeApiCall
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val ENV_EMAIL = "PICKII_TEST_EMAIL"
private const val ENV_PASSWORD = "PICKII_TEST_PASSWORD"

/**
 * 실제 백엔드(Railway 프로덕션)에 붙는 수동 통합 테스트.
 *
 * **일반 테스트 실행에 안 낀다.** `:shared:testAndroidHostTest`/`:app:testDebugUnitTest`에서
 * 명시적으로 제외되고(`shared/build.gradle.kts` 참고), `./gradlew :shared:backendIntegrationTest`로만
 * 실행된다. 자격증명(`PICKII_TEST_EMAIL`/`PICKII_TEST_PASSWORD` 환경변수)이 없으면 실패가 아니라
 * **스킵**된다 — 다른 사람이 클론했을 때 이 자격증명 없이도 빌드/일반 테스트가 깨지면 안 되기 때문.
 *
 * 확인하는 것은 딱 하나: Ktor로 전환한 [KtorAuthApiService] + [safeApiCall] + [ApiEnvelope] 조합이
 * 실제 응답과 맞물려서, Retrofit 때와 동일한 반환 계약(`Result<T>` + `ApiException(code, message)`)을
 * 유지하는지. 프로덕션 데이터를 만드는 POST(회원가입 등)는 넣지 않는다 — 로그인(읽기성 인증)과
 * 실패 케이스만 확인한다.
 */
class AuthApiServiceBackendIntegrationTest {
    @Test
    fun `실제 이메일 로그인이 성공하면 토큰이 파싱된다`() =
        runTest {
            val email = System.getenv(ENV_EMAIL)
            val password = System.getenv(ENV_PASSWORD)
            assumeTrue(
                "$ENV_EMAIL/$ENV_PASSWORD 환경변수가 없어서 스킵 — 실제 계정 자격증명을 넣어야 실행됨",
                email != null && password != null
            )

            val authApiService = KtorAuthApiService(backendIntegrationTestHttpClient())
            val result =
                safeApiCall<ApiEnvelope<LoginResponseDto>> {
                    authApiService.login(
                        LoginRequest(
                            email = email!!,
                            password = password!!,
                            autoLogin = false,
                            deviceId = NoOpAuthSession().deviceId()
                        )
                    )
                }

            val envelope =
                result.getOrElse {
                    throw AssertionError("로그인 실패 — 자격증명이 맞는지, 백엔드가 응답 가능한지 확인 필요: $it", it)
                }
            assertTrue(envelope.data.accessToken.isNotBlank(), "accessToken이 비어있음")
            assertTrue(envelope.data.refreshToken.isNotBlank(), "refreshToken이 비어있음")
        }

    @Test
    fun `틀린 비밀번호면 Retrofit 때와 같은 모양의 ApiException으로 실패한다`() =
        runTest {
            val email = System.getenv(ENV_EMAIL)
            assumeTrue("$ENV_EMAIL 환경변수가 없어서 스킵", email != null)

            val authApiService = KtorAuthApiService(backendIntegrationTestHttpClient())
            val result =
                safeApiCall<ApiEnvelope<LoginResponseDto>> {
                    authApiService.login(
                        LoginRequest(
                            email = email!!,
                            password = "definitely-wrong-password-${System.currentTimeMillis()}",
                            autoLogin = false,
                            deviceId = NoOpAuthSession().deviceId()
                        )
                    )
                }

            val exception = result.exceptionOrNull()
            assertIs<ApiException>(exception, "틀린 비밀번호인데 ApiException이 아닌 결과가 나옴: $result")
            assertTrue(exception.code.isNotBlank(), "에러 code가 비어있음")
        }
}
