package com.example.pickii.di

import com.example.pickii.data.repository.CalendarApiRepository
import com.example.pickii.domain.repository.CalendarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 캘린더 저장소 구현체를 주입한다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CalendarRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCalendarRepository(calendarApiRepository: CalendarApiRepository): CalendarRepository
}
