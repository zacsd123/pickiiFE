package com.example.pickii.di

import com.example.pickii.data.repository.CalendarApiRepository
import com.example.pickii.domain.repository.CalendarRepository
import org.koin.dsl.module

/** 캘린더 저장소 구현체를 주입하는 Koin 모듈 (Hilt `CalendarRepositoryModule`을 대체). */
val calendarRepositoryModule =
    module {
        single<CalendarRepository> { CalendarApiRepository(apiService = get(), json = get()) }
    }
