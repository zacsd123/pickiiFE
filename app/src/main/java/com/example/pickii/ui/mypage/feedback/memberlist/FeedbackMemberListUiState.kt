package com.example.pickii.ui.mypage.feedback.memberlist

import com.example.pickii.domain.model.FeedbackTeamMember

/** [FeedbackMemberListScreen]에 표시되는 상태. */
data class FeedbackMemberListUiState(
    val isLoading: Boolean = true,
    val members: List<FeedbackTeamMember> = emptyList()
)
