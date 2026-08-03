package com.example.pickii.ui.mypage.home

/** [MyPageHomeScreen]에 표시되는 상태. */
data class MyPageHomeUiState(
    val isLoading: Boolean = true,
    /** 프로필(이력서) 보유 여부. 아직 조회 전이면 null. */
    val hasProfile: Boolean? = null,
    val nickname: String = "",
    val exp: Int = 0
)
