package com.example.pickii.di

import com.example.pickii.data.repository.ProfileApiRepository
import com.example.pickii.data.repository.RecruitApiRepository
import com.example.pickii.data.repository.RecruitAuthSessionRepository
import com.example.pickii.data.repository.RecruitMasterDataRepository
import com.example.pickii.data.repository.SignupApiRepository
import com.example.pickii.domain.repository.MasterDataRepository
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
}
