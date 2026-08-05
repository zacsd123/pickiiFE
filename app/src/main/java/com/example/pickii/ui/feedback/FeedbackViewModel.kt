package com.example.pickii.ui.feedback

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        FeedbackUiState(
            selectedTab = FeedbackTabType.WRITE,
            projects = createMockProjects(),
        ),
    )

    val uiState = _uiState.asStateFlow()

    fun selectTab(tab: FeedbackTabType) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedTab = tab,
            )
        }
    }

    fun completeMemberFeedback(
        projectId: Long,
        memberId: Long,
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                projects = currentState.projects.map { project ->
                    if (project.id != projectId) {
                        project
                    } else {
                        project.copy(
                            members = project.members.map { member ->
                                if (member.id == memberId) {
                                    member.copy(isCompleted = true)
                                } else {
                                    member
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    private fun createMockProjects(): List<FeedbackProjectUiModel> {
        return listOf(
            FeedbackProjectUiModel(
                id = 1L,
                title = "서비스 기획 공모전",
                startDate = "7월 4일",
                endDate = "7월 6일",
                remainingDays = 2,
                completedCount = 1,
                totalCount = 3,
                canReadFeedback = true,
                members = listOf(
                    FeedbackMemberUiModel(
                        id = 1L,
                        name = "김OO님",
                        isCompleted = false,
                    ),
                    FeedbackMemberUiModel(
                        id = 2L,
                        name = "이OO님",
                        isCompleted = false,
                    ),
                    FeedbackMemberUiModel(
                        id = 3L,
                        name = "박OO님",
                        isCompleted = true,
                    ),
                ),
            ),
            FeedbackProjectUiModel(
                id = 2L,
                title = "서비스 기획 공모전",
                startDate = "7월 4일",
                endDate = "7월 6일",
                remainingDays = 2,
                completedCount = 1,
                totalCount = 3,
                canReadFeedback = false,
                members = listOf(
                    FeedbackMemberUiModel(
                        id = 4L,
                        name = "김OO님",
                        isCompleted = false,
                    ),
                    FeedbackMemberUiModel(
                        id = 5L,
                        name = "이OO님",
                        isCompleted = false,
                    ),
                    FeedbackMemberUiModel(
                        id = 6L,
                        name = "박OO님",
                        isCompleted = true,
                    ),
                ),
            ),
        )
    }
}
