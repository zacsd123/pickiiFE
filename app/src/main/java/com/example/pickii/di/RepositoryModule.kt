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
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.domain.repository.SignupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Repository 인터페이스와 실제(백엔드 연동) 구현체를 연결하는 Hilt 모듈. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /** [SessionRepository]를 [RecruitAuthSessionRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: RecruitAuthSessionRepository): SessionRepository

    /** [RecruitRepository]를 [RecruitApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindRecruitRepository(impl: RecruitApiRepository): RecruitRepository

    /** [MasterDataRepository]를 [RecruitMasterDataRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindMasterDataRepository(impl: RecruitMasterDataRepository): MasterDataRepository

    /** [SignupRepository]를 [SignupApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindSignupRepository(impl: SignupApiRepository): SignupRepository

    /** [ProfileRepository]를 [ProfileApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileApiRepository): ProfileRepository

    /** [MyPageActivityRepository]를 [MyPageActivityApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindMyPageActivityRepository(impl: MyPageActivityApiRepository): MyPageActivityRepository

    /** [AccountRepository]를 [AccountApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountApiRepository): AccountRepository

    /** [NotificationSettingsRepository]를 [NotificationSettingsApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindNotificationSettingsRepository(
        impl: NotificationSettingsApiRepository
    ): NotificationSettingsRepository

    /** [FeedbackRepository]를 [FeedbackApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(impl: FeedbackApiRepository): FeedbackRepository

    /** [ChatRepository]를 [ChatApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatApiRepository): ChatRepository

    /** [ApplicantRepository]를 [ApplicantApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindApplicantRepository(impl: ApplicantApiRepository): ApplicantRepository

    /** [NotificationRepository]를 [NotificationApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationApiRepository): NotificationRepository

    /** [MeetingPollRepository]를 [MeetingPollApiRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindMeetingPollRepository(impl: MeetingPollApiRepository): MeetingPollRepository
}
