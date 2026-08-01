package com.example.pickii.ui.chat

data class ChatProjectInfoUiModel(
    val projectTitle: String,
    val startDate: String,
    val endDate: String,
    val memberCount: Int,
    val leaderName: String,
    val progressPercent: Int,
    val projectStatus: ProjectStatus,
)