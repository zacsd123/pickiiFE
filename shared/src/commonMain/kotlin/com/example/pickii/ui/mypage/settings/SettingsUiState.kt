package com.example.pickii.ui.mypage.settings

/** [SettingsScreen]의 계정 설정 섹션에 표시되는 상태. */
data class SettingsUiState(
    val isKakaoLinked: Boolean = false,
    val isUnlinkConfirmVisible: Boolean = false,
    val pendingUnlinkProvider: String? = null
)
