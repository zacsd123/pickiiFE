package com.example.pickii.ui.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

private enum class FeedbackScreenType {
    LIST,
    WRITE,
    DETAIL,
}

@Composable
fun FeedbackRoute(
    onExit: () -> Unit,
    onNotificationClick: () -> Unit = {},
    viewModel: FeedbackViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentScreen by remember {
        mutableStateOf(FeedbackScreenType.LIST)
    }

    var selectedProjectId by remember {
        mutableStateOf<Long?>(null)
    }

    var selectedMemberId by remember {
        mutableStateOf<Long?>(null)
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
                    currentScreen = FeedbackScreenType.DETAIL
                },
                onNotificationClick = onNotificationClick,
                onCloseClick = onExit,
            )
        }

        FeedbackScreenType.WRITE -> {
            val selectedProject = uiState.projects.firstOrNull { project ->
                project.id == selectedProjectId
            }

            val selectedMember = selectedProject
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
                onSubmitClick = {
                    val projectId = selectedProjectId
                    val memberId = selectedMemberId

                    if (projectId != null && memberId != null) {
                        viewModel.completeMemberFeedback(
                            projectId = projectId,
                            memberId = memberId,
                        )
                    }

                    currentScreen = FeedbackScreenType.LIST
                },
            )
        }

        FeedbackScreenType.DETAIL -> {
            FeedbackDetailScreen(
                uiState = FeedbackDetailUiState(
                    keywords = listOf(
                        "빠른일처리",
                        "열정",
                    ),
                    complimentSummary = buildString {
                        append("맡은 역할을 책임감 있게 수행하고, ")
                        append("팀원들과 적극적으로 소통했다는 의견이 많았습니다. ")
                        append("업무 진행 속도가 빠르고 프로젝트에 열정적으로 참여한 점이 강점으로 평가되었습니다.")
                    },
                    improvementSummary = buildString {
                        append("업무 진행 과정에서 본인의 의견을 조금 더 구체적으로 공유하면 ")
                        append("팀원들이 상황을 이해하고 협업하는 데 도움이 될 것 같다는 의견이 있었습니다.")
                    },
                ),
                onCloseClick = {
                    currentScreen = FeedbackScreenType.LIST
                },
                onNotificationClick = onNotificationClick,
            )
        }
    }
}
