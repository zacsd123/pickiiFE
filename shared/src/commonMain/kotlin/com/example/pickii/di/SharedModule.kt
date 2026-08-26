package com.example.pickii.di

import com.example.pickii.ui.navigation.MainNavigationViewModel
import com.example.pickii.ui.onboarding.OnboardingViewModel
import com.example.pickii.ui.passwordreset.PasswordResetViewModel
import com.example.pickii.ui.signup.SignupViewModel
import com.example.pickii.ui.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * commonMain으로 옮긴 화면의 Koin 바인딩을 담는 모듈. Android는 [com.example.pickii.PickiiApplication]의
 * `startKoin`에, iOS는 `initKoin()`에 각각 연결한다 — 두 플랫폼이 별도 프로세스라 Koin도 플랫폼별로
 * 한 번씩 시작해야 한다(Android의 `viewModelModule` 등 아직 app/에 남은 나머지 바인딩과는 별개).
 */
val sharedModule =
    module {
        viewModel { SplashViewModel() }
        viewModel { MainNavigationViewModel(sessionRepository = get()) }
        viewModel { OnboardingViewModel(masterDataRepository = get(), profileRepository = get()) }
        viewModel { SignupViewModel(signupRepository = get(), sessionRepository = get(), profileRepository = get()) }
        viewModel { PasswordResetViewModel(signupRepository = get()) }
    }
