package com.example.pickii.ui.feedback

data class FeedbackDetailUiState(
    val keywords: List<String> = emptyList(),
    val complimentSummary: String = "",
    val improvementSummary: String = "",
    val isLoading: Boolean = false
)
