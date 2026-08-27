package com.example.pickii.di

import com.example.pickii.data.repository.AccountApiRepository
import com.example.pickii.data.repository.ApplicantApiRepository
import com.example.pickii.data.repository.ChatApiRepository
import com.example.pickii.data.repository.FeedbackApiRepository
import com.example.pickii.data.repository.MeetingPollApiRepository
import com.example.pickii.data.repository.MyPageActivityApiRepository
import com.example.pickii.data.repository.NotificationApiRepository
import com.example.pickii.data.repository.NotificationSettingsApiRepository
import com.example.pickii.data.repository.ProfileApiRepository
import com.example.pickii.data.repository.ProjectApiRepository
import com.example.pickii.data.repository.RecruitApiRepository
import com.example.pickii.data.repository.RecruitAuthSessionRepository
import com.example.pickii.data.repository.RecruitMasterDataRepository
import com.example.pickii.data.repository.SignupApiRepository
import com.example.pickii.domain.repository.AccountRepository
import com.example.pickii.domain.repository.ApplicantRepository
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.FeedbackRepository
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.MeetingPollRepository
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.domain.repository.NotificationSettingsRepository
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.domain.repository.ProjectRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.domain.repository.SignupRepository
import org.koin.dsl.module

/**
 * Repository 인터페이스와 실제(백엔드 연동) 구현체를 연결하는 Koin 모듈.
 *
 * `ChatRepository`도 여기 있다 — 원래는 사진 업로드가 `android.content.Context`/`Uri`를 직접 써서
 * app 전용으로 남겨뒀는데, `uploadImage`가 검증·변환이 끝난 바이트만 받도록 시그니처를 바꾸면서
 * (`ChatRepository.kt` 참고) `ChatApiRepository` 자체는 완전히 플랫폼 무관해졌다. STOMP 실시간
 * 송수신(`ChatStompClient`)과 화면(`ChatRoomScreen`/`ChatRoomViewModel`)은 여전히 app 전용이지만,
 * REST 기반 채팅 동작(방 목록 조회, 1:1 채팅방 생성 등)은 iOS에서도 이미 동작한다.
 */
val sharedRepositoryModule =
    module {
        single<ChatRepository> {
            ChatApiRepository(
                chatApiService = get(),
                sessionRepository = get(),
                projectRepository = get()
            )
        }
        single<SessionRepository> {
            RecruitAuthSessionRepository(
                authApiService = get(),
                tokenStore = get(),
                deviceIdProvider = get(),
                profileRepository = get(),
                notificationRepository = get()
            )
        }
        single<RecruitRepository> {
            RecruitApiRepository(recruitApiService = get(), masterDataRepository = get())
        }
        single<MasterDataRepository> { RecruitMasterDataRepository(masterDataApiService = get()) }
        single<SignupRepository> { SignupApiRepository(authApiService = get()) }
        single<ProfileRepository> { ProfileApiRepository(profileApiService = get()) }
        single<MyPageActivityRepository> { MyPageActivityApiRepository(apiService = get()) }
        single<AccountRepository> { AccountApiRepository(authApiService = get()) }
        single<NotificationSettingsRepository> { NotificationSettingsApiRepository(apiService = get()) }
        single<FeedbackRepository> { FeedbackApiRepository(apiService = get()) }
        single<ApplicantRepository> { ApplicantApiRepository(apiService = get()) }
        single<NotificationRepository> { NotificationApiRepository(apiService = get()) }
        single<MeetingPollRepository> { MeetingPollApiRepository(apiService = get()) }
        single<ProjectRepository> { ProjectApiRepository(apiService = get()) }
    }
