package com.example.pickii.di

import com.example.pickii.ui.applicant.ApplicantListViewModel
import com.example.pickii.ui.calendar.category.ScheduleCategoryViewModel
import com.example.pickii.ui.calendar.daily.DailyCalendarViewModel
import com.example.pickii.ui.calendar.editor.ScheduleEditorViewModel
import com.example.pickii.ui.calendar.monthly.MonthlyCalendarViewModel
import com.example.pickii.ui.chat.ChatListViewModel
import com.example.pickii.ui.chat.ChatRoomViewModel
import com.example.pickii.ui.feedback.FeedbackViewModel
import com.example.pickii.ui.login.LoginViewModel
import com.example.pickii.ui.mypage.applications.ApplicationsViewModel
import com.example.pickii.ui.mypage.home.MyPageHomeViewModel
import com.example.pickii.ui.mypage.mycomments.MyCommentsViewModel
import com.example.pickii.ui.mypage.myrecruits.MyRecruitsViewModel
import com.example.pickii.ui.mypage.profile.edit.ProfileEditViewModel
import com.example.pickii.ui.mypage.scraps.ScrapsViewModel
import com.example.pickii.ui.mypage.settings.LogoutViewModel
import com.example.pickii.ui.mypage.settings.NotificationSettingsViewModel
import com.example.pickii.ui.mypage.settings.PasswordChangeViewModel
import com.example.pickii.ui.mypage.settings.SettingsViewModel
import com.example.pickii.ui.mypage.withdrawal.WithdrawalViewModel
import com.example.pickii.ui.recruitapply.RecruitApplyViewModel
import com.example.pickii.ui.recruitdetail.RecruitDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 아직 app/에 남아있는 ViewModel 22개를 등록하는 Koin 모듈 (Hilt `@HiltViewModel`을 대체).
 * commonMain으로 옮긴 화면의 ViewModel은 [com.example.pickii.di.sharedModule]에 따로 등록한다
 * (SplashViewModel이 첫 사례).
 *
 * `SavedStateHandle`을 받는 5개(RecruitDetail/RecruitApply/RecruitForm/ApplicantList/MemberProfile)는
 * 여기서 따로 처리할 게 없다 — Koin의 Android `koinViewModel()`이 Hilt와 동일하게 SavedStateHandle을
 * 스코프에 자동으로 넣어줘서 생성자의 `get()` 하나가 알아서 그걸 받는다. 화면을 나중에 commonMain으로
 * 옮길 때는 SavedStateHandle 자체가 Android 전용이라 이 부분도 같이 재검토해야 한다
 * (KMP_MIGRATION_PLAN.md Phase 1 표 참고, "Navigation 인자로 직접 전달" 방식으로 바뀔 가능성 있음).
 */
val viewModelModule =
    module {
        viewModel { LoginViewModel(sessionRepository = get(), profileRepository = get()) }
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
        viewModel {
            RecruitDetailViewModel(
                savedStateHandle = get(),
                recruitRepository = get(),
                sessionRepository = get(),
                chatRepository = get()
            )
        }
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
        viewModel { FeedbackViewModel(feedbackRepository = get()) }
        viewModel { MonthlyCalendarViewModel(calendarRepository = get(), notificationRepository = get()) }
        viewModel { DailyCalendarViewModel(calendarRepository = get()) }
        viewModel { ScheduleEditorViewModel(calendarRepository = get()) }
        viewModel { ScheduleCategoryViewModel(calendarRepository = get()) }
        viewModel { ChatListViewModel(chatRepository = get(), notificationRepository = get()) }
        viewModel {
            ChatRoomViewModel(
                chatRepository = get(),
                chatStompClient = get(),
                sessionRepository = get(),
                meetingPollRepository = get(),
                calendarRepository = get(),
                projectRepository = get(),
                activeChatRoomTracker = get(),
                savedMeetingScheduleStore = get()
            )
        }
    }
