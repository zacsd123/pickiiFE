package com.example.pickii.ui.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pickii.ui.common.LocalSnackbarHostState
import com.example.pickii.ui.common.RecruitUiEvent
import org.jetbrains.compose.resources.getString
import org.koin.androidx.compose.koinViewModel

private enum class FeedbackScreenType {
    LIST,
    WRITE,
    DETAIL
}

@Composable
fun FeedbackRoute(
    onExit: () -> Unit,
    onNotificationClick: () -> Unit = {},
    viewModel: FeedbackViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    var currentScreen by remember {
        mutableStateOf(FeedbackScreenType.LIST)
    }

    var selectedProjectId by remember {
        mutableStateOf<Long?>(null)
    }

    var selectedMemberId by remember {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RecruitUiEvent.ShowToast ->
                    snackbarHostState.showSnackbar(getString(event.messageRes))
            }
        }
    }

    when (currentScreen) {
        FeedbackScreenType.LIST -> {
            FeedbackScreen(
                uiState = uiState,
                onTabClick = viewModel::selectTab,
                onWriteFeedbackClick = { projectId, memberId ->
                    selectedProjectId = projectId
                    selectedMemberId = memberId
                    currentScreen = FeedbackScreenType.WRITE
                },
                onReceivedFeedbackClick = { projectId ->
                    selectedProjectId = projectId
                    selectedMemberId = null
                    viewModel.loadAiFeedback(projectId)
                    currentScreen = FeedbackScreenType.DETAIL
                },
                onNotificationClick = onNotificationClick,
                onCloseClick = onExit
            )
        }

        FeedbackScreenType.WRITE -> {
            val selectedProject =
                uiState.projects.firstOrNull { project ->
                    project.id == selectedProjectId
                }

            val selectedMember =
                selectedProject
                    ?.members
                    ?.firstOrNull { member ->
                        member.id == selectedMemberId
                    }

            FeedbackWriteScreen(
                projectTitle = selectedProject?.title.orEmpty(),
                memberName = selectedMember?.name.orEmpty(),
                onBackClick = {
                    currentScreen = FeedbackScreenType.LIST
                },
                onSubmitClick = { input ->
                    val projectId = selectedProjectId
                    val memberId = selectedMemberId

                    if (projectId != null && memberId != null) {
                        viewModel.submitFeedback(
                            projectId = projectId,
                            memberId = memberId,
                            input = input,
                            onComplete = { currentScreen = FeedbackScreenType.LIST }
                        )
                    }
                }
            )
        }

        FeedbackScreenType.DETAIL -> {
            FeedbackDetailScreen(
                uiState = uiState.aiFeedback,
                onCloseClick = {
                    currentScreen = FeedbackScreenType.LIST
                },
                onNotificationClick = onNotificationClick
            )
        }
    }
}
