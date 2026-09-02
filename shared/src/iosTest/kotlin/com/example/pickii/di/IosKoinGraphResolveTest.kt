package com.example.pickii.di

import androidx.lifecycle.SavedStateHandle
import com.example.pickii.ui.applicant.ApplicantListViewModel
import com.example.pickii.ui.calendar.category.ScheduleCategoryViewModel
import com.example.pickii.ui.calendar.daily.DailyCalendarViewModel
import com.example.pickii.ui.calendar.editor.ScheduleEditorViewModel
import com.example.pickii.ui.calendar.monthly.MonthlyCalendarViewModel
import com.example.pickii.ui.chat.ChatListViewModel
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
 * 네트워크는 필요 없다 — `HttpClient`/리포지토리 인스턴스를 "만들 수 있는가"만 보는 테스트라
 * 백엔드가 꺼져 있어도 통과해야 정상이다(실제 API 호출은 `viewModelScope.launch { }` 안에서
 * 일어나는데 여기서는 그 코루틴을 실행시키지 않는다 — Android `KoinGraphResolveTest`와 동일 원리).
 *
 * shared/commonMain으로 화면을 옮길 때마다 여기 `get<...ViewModel>()` 줄도 같이 추가할 것.
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

    @Test
    fun `shared 모듈의 ViewModel 전부가 iOS Koin 그래프에서 resolve된다`() {
        val koin = KoinPlatformTools.defaultContext().get()
        koin.get<SplashViewModel>()
        koin.get<LoginViewModel>()
        koin.get<MainNavigationViewModel>()
        koin.get<OnboardingViewModel>()
        koin.get<SignupViewModel>()
        koin.get<PasswordResetViewModel>()
        koin.get<HomeViewModel>()
        koin.get<NotificationViewModel>()
        koin.get<MemberProfileViewModel>()
        koin.get<ProfileViewModel>()
        koin.get<RecruitDetailViewModel>()
        koin.get<RecruitFormViewModel>()
        koin.get<MyPageHomeViewModel>()
        koin.get<ProfileEditViewModel>()
        koin.get<ApplicationsViewModel>()
        koin.get<MyCommentsViewModel>()
        koin.get<MyRecruitsViewModel>()
        koin.get<ScrapsViewModel>()
        koin.get<SettingsViewModel>()
        koin.get<LogoutViewModel>()
        koin.get<PasswordChangeViewModel>()
        koin.get<NotificationSettingsViewModel>()
        koin.get<WithdrawalViewModel>()
        koin.get<MonthlyCalendarViewModel>()
        koin.get<DailyCalendarViewModel>()
        koin.get<ScheduleEditorViewModel>()
        koin.get<ScheduleCategoryViewModel>()
        koin.get<FeedbackViewModel>()
        koin.get<RecruitApplyViewModel>()
        koin.get<ApplicantListViewModel>()
        koin.get<ChatListViewModel>()
    }
}
