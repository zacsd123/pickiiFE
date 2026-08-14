package com.example.pickii.ui.feedback

enum class FeedbackTabType {
    WRITE,
    RECEIVED
}

data class FeedbackUiState(
    val isLoading: Boolean = false,
    val selectedTab: FeedbackTabType = FeedbackTabType.WRITE,
    val projects: List<FeedbackProjectUiModel> = emptyList(),
    val aiFeedback: FeedbackDetailUiState = FeedbackDetailUiState()
)

data class FeedbackProjectUiModel(
    val id: Long,
    val title: String,
    val startDate: String,
    val endDate: String,
    val remainingDays: Int,
    val completedCount: Int,
    val totalCount: Int,
    val canReadFeedback: Boolean,
    val members: List<FeedbackMemberUiModel> = emptyList()
)

data class FeedbackMemberUiModel(
    val id: Long,
    val name: String,
    val isCompleted: Boolean
)
