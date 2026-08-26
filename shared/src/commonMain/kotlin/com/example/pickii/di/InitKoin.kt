package com.example.pickii.di

import org.koin.core.context.startKoin

/**
 * iOS 전용 Koin 부트스트랩. Android는 PickiiApplication.onCreate()의 startKoin이 이미 이 역할을
 * 한다 — iOS는 별도 프로세스(별도 앱 바이너리)라 Koin의 GlobalContext를 공유하지 않으므로, iOS
 * 앱 진입 시점(iOSApp.swift의 init())에 한 번 따로 불러줘야 한다.
 */
fun initKoin() {
    startKoin {
        modules(sharedInfraModule, sharedNetworkModule, sharedRepositoryModule, sharedCalendarRepositoryModule, sharedModule)
    }
}
