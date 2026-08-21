package com.example.pickii.di

// 스파이크용 임시 제거, 버전 정책 결정 후 복구 (koin-core iOS klib ABI 불일치로
// :shared:compileKotlinIosSimulatorArm64 진단을 막고 있어서 비활성화함).
// import org.koin.dsl.module
//
// /**
//  * Koin으로 옮길 바인딩을 담을 자리. 지금은 앱이 여전히 Hilt로 전체 DI 그래프를 구성하므로
//  * 이 모듈은 아직 `Application`에 연결돼 있지 않다 — Login/Home을 실제로 이식하는 다음 단계에서
//  * [com.example.pickii.domain.repository.SessionRepository] 등의 바인딩을 채우고 Hilt와 나란히(또는
//  * 대체해) 연결한다.
//  */
// val sharedModule =
//     module {
//     }
