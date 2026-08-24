package com.example.pickii.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.SavedMeetingScheduleStore
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.notification.ActiveChatRoomTracker
import com.example.pickii.data.notification.FcmTokenRegistrar
import com.example.pickii.data.remote.socket.ChatStompClient
import com.example.pickii.ui.applicant.ApplicantListViewModel
import com.example.pickii.ui.calendar.category.ScheduleCategoryViewModel
import com.example.pickii.ui.calendar.daily.DailyCalendarViewModel
import com.example.pickii.ui.calendar.editor.ScheduleEditorViewModel
import com.example.pickii.ui.calendar.monthly.MonthlyCalendarViewModel
import com.example.pickii.ui.chat.ChatListViewModel
import com.example.pickii.ui.chat.ChatRoomViewModel
import com.example.pickii.ui.feedback.FeedbackViewModel
import com.example.pickii.ui.home.HomeViewModel
import com.example.pickii.ui.login.LoginViewModel
import com.example.pickii.ui.memberprofile.MemberProfileViewModel
import com.example.pickii.ui.mypage.applications.ApplicationsViewModel
import com.example.pickii.ui.mypage.home.MyPageHomeViewModel
import com.example.pickii.ui.mypage.mycomments.MyCommentsViewModel
import com.example.pickii.ui.mypage.myrecruits.MyRecruitsViewModel
import com.example.pickii.ui.mypage.profile.ProfileViewModel
import com.example.pickii.ui.mypage.profile.edit.ProfileEditViewModel
import com.example.pickii.ui.mypage.scraps.ScrapsViewModel
import com.example.pickii.ui.mypage.settings.LogoutViewModel
import com.example.pickii.ui.mypage.settings.NotificationSettingsViewModel
import com.example.pickii.ui.mypage.settings.PasswordChangeViewModel
import com.example.pickii.ui.mypage.settings.SettingsViewModel
import com.example.pickii.ui.mypage.withdrawal.WithdrawalViewModel
import com.example.pickii.ui.navigation.ARG_MEMBER_ID
import com.example.pickii.ui.navigation.ARG_POST_ID
import com.example.pickii.ui.navigation.MainNavigationViewModel
import com.example.pickii.ui.notification.NotificationViewModel
import com.example.pickii.ui.onboarding.OnboardingViewModel
import com.example.pickii.ui.passwordreset.PasswordResetViewModel
import com.example.pickii.ui.recruitapply.RecruitApplyViewModel
import com.example.pickii.ui.recruitdetail.RecruitDetailViewModel
import com.example.pickii.ui.recruitform.RecruitFormViewModel
import com.example.pickii.ui.signup.SignupViewModel
import com.example.pickii.ui.splash.SplashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito
import kotlin.test.assertSame

/**
 * Koin 4.1.1의 `Module.verify()`는 static 검사라 순환·런타임 문제를 놓칠 수 있다(JVM 리플렉션 기반
 * 타입 체크일 뿐, 실제로 인스턴스를 만들어보지 않는다) — 그래서 여기서는 실제 프로덕션 모듈
 * (`infraModule`/`networkModule`/`repositoryModule`/`calendarRepositoryModule`/`viewModelModule`)을
 * 그대로 실행해서 32개 ViewModel을 하나씩 진짜로 resolve해본다.
 *
 * `Context`는 Mockito로 만든다 — 실제 Android/Robolectric 없이 순수 JVM 테스트에서 `Context`
 * 타입이 필요한 생성자를 만족시키기 위함이고(`TokenStore` 등은 생성자에서 `Context`를 저장만 하고
 * DataStore 파일 접근은 실제 메서드 호출 시점에만 일어나므로 mock으로 충분하다), `SavedStateHandle`은
 * postId/memberId를 미리 채워서 5개 ViewModel의 `requireNotNull(savedStateHandle[...])` 체크를
 * 통과시킨다(실제 앱에서는 Koin의 Android `koinViewModel()`이 자동으로 채워주는 값).
 *
 * `Dispatchers.Main`을 `StandardTestDispatcher`로 바꾸는 이유: 22개 ViewModel의 `init { }`이
 * `viewModelScope.launch { ... }`로 실제 리포지토리를 호출하는데, `StandardTestDispatcher`는
 * `advanceUntilIdle()`을 부르기 전까진 launch된 코루틴 본문을 실행하지 않는다 — 그래서 이 테스트는
 * "그래프가 resolve되는지"만 보고, 실제 네트워크 호출은 절대 실행되지 않는다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KoinGraphResolveTest : KoinTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        startKoin {
            modules(
                module {
                    single<Context> {
                        // TokenStore/DeviceIdProvider/SavedMeetingScheduleStore가 물고 있는
                        // `preferencesDataStore` 델리게이트는 프로퍼티 초기화 시점(=생성자 실행 중)에
                        // `context.applicationContext.filesDir`로 파일 경로를 계산한다 — Mockito
                        // 기본 mock은 이 두 메서드가 null을 반환해서 그대로 두면 NPE가 난다.
                        val tempDir =
                            kotlin.io.path
                                .createTempDirectory(prefix = "koin-graph-resolve-test")
                                .toFile()
                        Mockito.mock(Context::class.java).also { context ->
                            Mockito.`when`(context.applicationContext).thenReturn(context)
                            Mockito.`when`(context.filesDir).thenReturn(tempDir)
                        }
                    }
                    single {
                        SavedStateHandle(
                            mapOf(ARG_POST_ID to "test-post-id", ARG_MEMBER_ID to "test-member-id")
                        )
                    }
                },
                infraModule,
                networkModule,
                repositoryModule,
                calendarRepositoryModule,
                viewModelModule
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `32개 ViewModel이 전부 Koin 그래프에서 resolve된다`() {
        get<SplashViewModel>()
        get<LoginViewModel>()
        get<OnboardingViewModel>()
        get<SignupViewModel>()
        get<PasswordResetViewModel>()
        get<HomeViewModel>()
        get<MainNavigationViewModel>()
        get<NotificationViewModel>()
        get<MemberProfileViewModel>()
        get<MyPageHomeViewModel>()
        get<ProfileViewModel>()
        get<ProfileEditViewModel>()
        get<ApplicationsViewModel>()
        get<MyCommentsViewModel>()
        get<MyRecruitsViewModel>()
        get<ScrapsViewModel>()
        get<SettingsViewModel>()
        get<LogoutViewModel>()
        get<PasswordChangeViewModel>()
        get<NotificationSettingsViewModel>()
        get<WithdrawalViewModel>()
        get<RecruitDetailViewModel>()
        get<RecruitApplyViewModel>()
        get<RecruitFormViewModel>()
        get<ApplicantListViewModel>()
        get<FeedbackViewModel>()
        get<MonthlyCalendarViewModel>()
        get<DailyCalendarViewModel>()
        get<ScheduleEditorViewModel>()
        get<ScheduleCategoryViewModel>()
        get<ChatListViewModel>()
        get<ChatRoomViewModel>()
    }

    /**
     * `di/InfraModule.kt`의 6개 싱글턴 전부 확인한다. 특히 [ChatStompClient]가 실수로 `factory`로
     * 바뀌면(또는 `single` 선언을 지우면) 채팅방 진입마다 웹소켓 연결이 새로 생겨서 메시지 중복 수신·
     * 한쪽 연결만 끊김 같은, 재현하기도 진단하기도 어려운 버그가 된다 — 이 테스트가 그걸 막는다.
     *
     * `AuthInterceptor`/`TokenAuthenticator`는 Retrofit 제거와 함께 삭제됐다 — 토큰 첨부/갱신은
     * 이제 `HttpClientFactory`의 Ktor `Auth` 플러그인이 전담한다.
     */
    @Test
    fun `인프라 싱글턴 6개 전부 진짜 싱글턴이다`() {
        assertSame(get<TokenStore>(), get<TokenStore>(), "TokenStore가 매번 새 인스턴스면 DataStore 인스턴스 중복 생성 위험")
        assertSame(
            get<DeviceIdProvider>(),
            get<DeviceIdProvider>(),
            "DeviceIdProvider가 매번 새 인스턴스면 DataStore 인스턴스 중복 생성 위험"
        )
        assertSame(
            get<SavedMeetingScheduleStore>(),
            get<SavedMeetingScheduleStore>(),
            "SavedMeetingScheduleStore가 매번 새 인스턴스면 DataStore 인스턴스 중복 생성 위험"
        )
        assertSame(
            get<ActiveChatRoomTracker>(),
            get<ActiveChatRoomTracker>(),
            "싱글턴이 아니면 FCM 알림 억제와 채팅방 진입 추적이 서로 다른 인스턴스를 봐서 어긋남"
        )
        assertSame(
            get<FcmTokenRegistrar>(),
            get<FcmTokenRegistrar>(),
            "싱글턴이 아니면 PickiiApplication과 FcmService가 서로 다른 lastRegisteredToken 캐시를 가져 중복 등록 방지가 깨짐"
        )
        assertSame(
            get<ChatStompClient>(),
            get<ChatStompClient>(),
            "싱글턴이 아니면 채팅방 진입마다 웹소켓 연결이 두 개 생겨 메시지 중복 수신/한쪽만 끊김으로 이어짐"
        )
    }
}
