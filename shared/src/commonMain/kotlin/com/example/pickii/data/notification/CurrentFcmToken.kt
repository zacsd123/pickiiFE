package com.example.pickii.data.notification

/**
 * 현재 기기의 FCM 푸시 토큰. Firebase Messaging은 Android 전용 SDK라 iOS는 아직 대응 구현이 없다
 * (APNs/GitLive SDK 연동은 Phase 5 범위) — iOS는 항상 null을 돌려주고, 호출부는 이미 이 값이
 * 없을 수 있다고 가정하고 만들어져 있다(`RecruitAuthSessionRepository.unregisterDeviceToken()`).
 */
expect suspend fun currentFcmToken(): String?
