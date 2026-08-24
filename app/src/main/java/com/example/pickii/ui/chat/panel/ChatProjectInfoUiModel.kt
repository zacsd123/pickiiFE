package com.example.pickii.ui.chat

import com.example.pickii.domain.model.ProjectStatus

data class ChatProjectInfoUiModel(
    val projectTitle: String,
    val startDate: String,
    val endDate: String,
    val memberCount: Int,
    val leaderName: String,
    val projectStatus: ProjectStatus
)
