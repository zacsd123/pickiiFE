package com.example.pickii.di

import com.example.pickii.data.repository.AccountApiRepository
import com.example.pickii.data.repository.ApplicantApiRepository
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
 * Repository 인터페이스와 실제(백엔드 연동) 구현체를 연결하는 Koin 모듈 — app의 `repositoryModule`에서
 * `ChatRepository`(아직 `android.content.Context`/`Uri`로 사진 업로드를 처리해서 Phase 5 범위)만
 * 뺀 13개를 옮겼다.
 */
val sharedRepositoryModule =
    module {
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
