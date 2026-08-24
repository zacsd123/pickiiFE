package com.example.pickii.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.domain.model.FeedbackProject
import com.example.pickii.domain.model.FeedbackScores
import com.example.pickii.domain.model.FeedbackTeamMember
import com.example.pickii.domain.repository.FeedbackRepository
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.feedback_toast_ai_feedback_failed
import com.example.pickii.shared.generated.resources.feedback_toast_already_evaluated
import com.example.pickii.shared.generated.resources.feedback_toast_load_failed
import com.example.pickii.shared.generated.resources.feedback_toast_submit_failed
import com.example.pickii.ui.common.RecruitUiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

private const val ERROR_CODE_ALREADY_EVALUATED = "ALREADY_EVALUATED"
private val FeedbackDateFormatter =
    LocalDate.Format {
        monthNumber(padding = Padding.NONE)
        char('월')
        char(' ')
        day(padding = Padding.NONE)
        char('일')
    }

class FeedbackViewModel
    constructor(
        private val feedbackRepository: FeedbackRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(FeedbackUiState())
        val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                feedbackRepository
                    .getMyFeedbackProjects()
                    .onSuccess { page ->
                        val projects = page.items.map { it.toUiModel() }
                        _uiState.update { it.copy(isLoading = false, projects = projects) }
                        loadWritableMembers(projects)
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                        emitEvent(RecruitUiEvent.ShowToast(Res.string.feedback_toast_load_failed))
                    }
            }
        }

        fun selectTab(tab: FeedbackTabType) {
            _uiState.update { it.copy(selectedTab = tab) }
        }

        /**
         * `GET /feedbacks`(4-11) 목록에는 팀원별 평가 대상이 없어, 프로젝트별로 `GET
         * /feedbacks/projects/{projectId}/members`(4-9)를 따로 호출해 채운다. 평가 기간이 지난
         * 프로젝트는 4-9가 실패할 수 있는데, 그 경우 해당 프로젝트는 팀원 목록만 비운 채 둔다(작성
         * 탭에서는 자연히 평가할 팀원이 없는 것으로 보인다).
         */
        private fun loadWritableMembers(projects: List<FeedbackProjectUiModel>) {
            projects.forEach { project ->
                viewModelScope.launch {
                    feedbackRepository
                        .getProjectMembers(project.id.toString())
                        .onSuccess { result ->
                            val members = result.members.map { it.toUiModel() }
                            _uiState.update { state ->
                                state.copy(
                                    projects =
                                        state.projects.map {
                                            if (it.id == project.id) it.copy(members = members) else it
                                        }
                                )
                            }
                        }
                }
            }
        }

        fun submitFeedback(
            projectId: Long,
            memberId: Long,
            input: FeedbackWriteInput,
            onComplete: () -> Unit
        ) {
            viewModelScope.launch {
                feedbackRepository
                    .submitFeedback(
                        projectId = projectId.toString(),
                        revieweeId = memberId.toString(),
                        scores =
                            FeedbackScores(
                                responsibility = input.responsibility,
                                communication = input.communication,
                                deadline = input.deadline,
                                cooperation = input.cooperation,
                                contribution = input.participation
                            ),
                        strength = input.strength,
                        weakness = input.weakness
                    ).onSuccess {
                        _uiState.update { state ->
                            state.copy(
                                projects =
                                    state.projects.map { project ->
                                        if (project.id != projectId) {
                                            project
                                        } else {
                                            project.copy(
                                                members =
                                                    project.members.map { member ->
                                                        if (member.id == memberId) {
                                                            member.copy(isCompleted = true)
                                                        } else {
                                                            member
                                                        }
                                                    }
                                            )
                                        }
                                    }
                            )
                        }
                        onComplete()
                    }.onFailure { error ->
                        val messageRes =
                            if (error is ApiException && error.code == ERROR_CODE_ALREADY_EVALUATED) {
                                Res.string.feedback_toast_already_evaluated
                            } else {
                                Res.string.feedback_toast_submit_failed
                            }
                        emitEvent(RecruitUiEvent.ShowToast(messageRes))
                    }
            }
        }

        fun loadAiFeedback(projectId: Long) {
            viewModelScope.launch {
                _uiState.update { it.copy(aiFeedback = it.aiFeedback.copy(isLoading = true)) }
                feedbackRepository
                    .getAiFeedback(projectId.toString())
                    .onSuccess { result ->
                        _uiState.update {
                            it.copy(
                                aiFeedback =
                                    FeedbackDetailUiState(
                                        // 명세 예시(#꼼꼼한)에 이미 "#"이 붙어 있어, 화면에서 또 붙이는 "#"과 겹치지 않게 벗겨낸다.
                                        keywords = result.keywords.map { keyword -> keyword.removePrefix("#") },
                                        complimentSummary = result.strengthSummary,
                                        improvementSummary = result.weaknessSummary,
                                        isLoading = false
                                    )
                            )
                        }
                    }.onFailure {
                        _uiState.update { it.copy(aiFeedback = FeedbackDetailUiState(isLoading = false)) }
                        emitEvent(RecruitUiEvent.ShowToast(Res.string.feedback_toast_ai_feedback_failed))
                    }
            }
        }

        private fun emitEvent(event: RecruitUiEvent) {
            _events.trySend(event)
        }

        private fun FeedbackProject.toUiModel(): FeedbackProjectUiModel =
            FeedbackProjectUiModel(
                id = projectId.toLong(),
                title = name,
                startDate = periodStart.format(FeedbackDateFormatter),
                endDate = periodEnd.format(FeedbackDateFormatter),
                remainingDays = remainingDays,
                completedCount = evaluatedCount,
                totalCount = memberCount,
                canReadFeedback = isAiFeedbackAvailable
            )

        private fun FeedbackTeamMember.toUiModel(): FeedbackMemberUiModel =
            FeedbackMemberUiModel(id = memberId.toLong(), name = nickname, isCompleted = isEvaluated)
    }
