package com.example.pickii.domain.model

/** 알림 설정 전체(`NotificationSetting` 테이블 1:1 대응, 9-5/9-6). */
data class NotificationSettings(
    val chatNoti: Boolean,
    val applicantNoti: Boolean,
    val commentNoti: Boolean,
    val scheduleNoti: Boolean,
    val matchNoti: Boolean,
    val projectNoti: Boolean,
    val marketingNoti: Boolean
) {
    /**
     * 마케팅 수신 동의를 제외한 나머지가 전부 켜져 있는지 여부. 설정 화면의 "전체 알림" 편의 토글에 쓰인다(서버 필드 아님).
     */
    val allEssentialEnabled: Boolean
        get() = chatNoti && applicantNoti && commentNoti && scheduleNoti && matchNoti && projectNoti
}
