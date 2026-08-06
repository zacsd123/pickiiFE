package com.example.pickii.ui.mypage.profile

import com.example.pickii.domain.model.MemberProfile

/** [ProfileViewScreen]에 표시되는 상태. */
data class ProfileViewUiState(
    val isLoading: Boolean = true,
    val profile: MemberProfile? = null,
    val topicLabels: List<String> = emptyList(),
    val toastMessageRes: Int? = null
)
