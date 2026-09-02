package com.example.pickii.di

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
import com.example.pickii.ui.navigation.MainNavigationViewModel
import com.example.pickii.ui.notification.NotificationViewModel
import com.example.pickii.ui.onboarding.OnboardingViewModel
import com.example.pickii.ui.passwordreset.PasswordResetViewModel
import com.example.pickii.ui.recruitapply.RecruitApplyViewModel
import com.example.pickii.ui.recruitdetail.RecruitDetailViewModel
import com.example.pickii.ui.recruitform.RecruitFormViewModel
import com.example.pickii.ui.signup.SignupViewModel
import com.example.pickii.ui.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * commonMain으로 옮긴 화면의 Koin 바인딩을 담는 모듈. Android는 [com.example.pickii.PickiiApplication]의
 * `startKoin`에, iOS는 `initKoin()`에 각각 연결한다 — 두 플랫폼이 별도 프로세스라 Koin도 플랫폼별로
 * 한 번씩 시작해야 한다(Android의 `viewModelModule` 등 아직 app/에 남은 나머지 바인딩과는 별개).
 */
val sharedModule =
    module {
        viewModel { SplashViewModel() }
        viewModel { LoginViewModel(sessionRepository = get(), profileRepository = get()) }
        viewModel { MainNavigationViewModel(sessionRepository = get()) }
        viewModel { OnboardingViewModel(masterDataRepository = get(), profileRepository = get()) }
        viewModel { SignupViewModel(signupRepository = get(), sessionRepository = get(), profileRepository = get()) }
        viewModel { PasswordResetViewModel(signupRepository = get()) }
        viewModel {
            HomeViewModel(
                recruitRepository = get(),
                masterDataRepository = get(),
                notificationRepository = get(),
                profileRepository = get(),
                sessionRepository = get()
            )
        }
        viewModel { NotificationViewModel(repository = get()) }
        viewModel {
            MemberProfileViewModel(
                savedStateHandle = get(),
                profileRepository = get(),
                masterDataRepository = get()
            )
        }
        viewModel { ProfileViewModel(profileRepository = get(), masterDataRepository = get()) }
        viewModel {
            RecruitDetailViewModel(
                savedStateHandle = get(),
                recruitRepository = get(),
                sessionRepository = get(),
                chatRepository = get()
            )
        }
        viewModel {
            RecruitFormViewModel(
                savedStateHandle = get(),
                recruitRepository = get(),
                masterDataRepository = get(),
                sessionRepository = get()
            )
        }
        viewModel { MyPageHomeViewModel(profileRepository = get(), notificationRepository = get()) }
        viewModel { ProfileEditViewModel(profileRepository = get(), masterDataRepository = get()) }
        viewModel { ApplicationsViewModel(repository = get()) }
        viewModel { MyCommentsViewModel(repository = get()) }
        viewModel { MyRecruitsViewModel(myPageActivityRepository = get(), recruitRepository = get()) }
        viewModel { ScrapsViewModel(myPageActivityRepository = get(), recruitRepository = get()) }
        viewModel { SettingsViewModel(accountRepository = get()) }
        viewModel { LogoutViewModel(sessionRepository = get()) }
        viewModel { PasswordChangeViewModel(accountRepository = get(), sessionRepository = get()) }
        viewModel { NotificationSettingsViewModel(repository = get()) }
        viewModel {
            WithdrawalViewModel(
                signupRepository = get(),
                accountRepository = get(),
                sessionRepository = get()
            )
        }
        viewModel { MonthlyCalendarViewModel(calendarRepository = get(), notificationRepository = get()) }
        viewModel { DailyCalendarViewModel(calendarRepository = get()) }
        viewModel { ScheduleEditorViewModel(calendarRepository = get()) }
        viewModel { ScheduleCategoryViewModel(calendarRepository = get()) }
        viewModel { FeedbackViewModel(feedbackRepository = get()) }
        viewModel {
            RecruitApplyViewModel(
                savedStateHandle = get(),
                recruitRepository = get(),
                sessionRepository = get(),
                masterDataRepository = get()
            )
        }
        viewModel {
            ApplicantListViewModel(
                repository = get(),
                recruitRepository = get(),
                chatRepository = get(),
                savedStateHandle = get()
            )
        }
        viewModel { ChatListViewModel(chatRepository = get(), notificationRepository = get()) }
    }
