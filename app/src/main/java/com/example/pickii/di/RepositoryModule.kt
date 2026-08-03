package com.example.pickii.di

import com.example.pickii.data.repository.MockRecruitRepository
import com.example.pickii.data.repository.MockSessionRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Repository 인터페이스와 목업 구현체를 연결하는 Hilt 모듈. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /** [SessionRepository]를 [MockSessionRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: MockSessionRepository): SessionRepository

    /** [RecruitRepository]를 [MockRecruitRepository]로 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindRecruitRepository(impl: MockRecruitRepository): RecruitRepository
}
