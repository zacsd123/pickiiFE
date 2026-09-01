package com.example.pickii.di

import com.example.pickii.ui.chat.ChatListViewModel
import com.example.pickii.ui.chat.ChatRoomViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 아직 app/에 남아있는 ViewModel 2개를 등록하는 Koin 모듈 (Hilt `@HiltViewModel`을 대체).
 * commonMain으로 옮긴 화면의 ViewModel은 [com.example.pickii.di.sharedModule]에 따로 등록한다
 * (SplashViewModel이 첫 사례).
 */
val viewModelModule =
    module {
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
                savedMeetingScheduleStore = get(),
                context = get()
            )
        }
    }
