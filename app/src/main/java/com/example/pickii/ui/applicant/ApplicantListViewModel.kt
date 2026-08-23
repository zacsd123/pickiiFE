package com.example.pickii.ui.applicant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.model.ApplicantEntry
import com.example.pickii.domain.model.ApplyStatus
import com.example.pickii.domain.repository.ApplicantRepository
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.navigation.ARG_POST_ID
import com.example.pickii.util.toDisplayString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 그룹 채팅 개설 시 기본 팀명으로 붙이는 접미사. */
private const val DEFAULT_TEAM_NAME_SUFFIX = " 팀"

/** 공고 지원자 목록(4-7) 조회, 수락/거절(4-8), 채팅방 개설(6-1, 8-3)을 담당한다. */
class ApplicantListViewModel
    constructor(
        private val repository: ApplicantRepository,
        private val recruitRepository: RecruitRepository,
        private val chatRepository: ChatRepository,
        savedStateHandle: SavedStateHandle
    ) : ViewModel() {
        private val recruitId: String = requireNotNull(savedStateHandle[ARG_POST_ID]) { "postId가 필요합니다." }

        private val _uiState = MutableStateFlow(ApplicantListUiState())
        val uiState: StateFlow<ApplicantListUiState> = _uiState.asStateFlow()

        private val _navigateToChatRoom = Channel<String>(Channel.BUFFERED)

        /** 채팅방 개설이 성공했을 때 이동할 채팅방 id(1회성 이벤트). */
        val navigateToChatRoom: Flow<String> = _navigateToChatRoom.receiveAsFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        init {
            refresh()
        }

        private fun refresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                repository
                    .getApplicants(recruitId)
                    .onSuccess { applicants ->
                        val uiApplicants = applicants.map { entry -> entry.toUiModel() }
                        _uiState.update { it.copy(isLoading = false, applicants = uiApplicants) }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(RecruitUiEvent.ShowToast(R.string.applicant_list_toast_load_failed))
                    }
            }
        }

        /** 지원자를 수락한다. */
        fun acceptApplicant(applicantId: Long) = updateApplyStatus(applicantId, accept = true)

        /** 지원자를 거절한다. */
        fun rejectApplicant(applicantId: Long) = updateApplyStatus(applicantId, accept = false)

        /** id로 지원자를 찾는다. */
        fun getApplicant(applicantId: Long): ApplicantUiModel? = uiState.value.applicants.find { it.id == applicantId }

        /** 수락된 지원자들과 단체 채팅방(프로젝트)을 개설한다(6-1). */
        fun createGroupChat() {
            viewModelScope.launch {
                val postTitle = recruitRepository.getPostById(recruitId).getOrNull()?.title
                val teamName = (postTitle ?: return@launch) + DEFAULT_TEAM_NAME_SUFFIX
                recruitRepository
                    .createProject(recruitId, teamName)
                    .onSuccess { result -> _navigateToChatRoom.send(result.chatRoomId) }
                    .onFailure {
                        _events.send(RecruitUiEvent.ShowToast(R.string.applicant_list_toast_group_chat_failed))
                    }
            }
        }

        /** 특정 지원자와 1:1 채팅방을 개설한다. */
        fun createDirectChat(memberId: Long) {
            viewModelScope.launch {
                chatRepository
                    .createDirectChatRoom(memberId)
                    .onSuccess { result -> _navigateToChatRoom.send(result.chatRoomId.toString()) }
                    .onFailure {
                        _events.send(RecruitUiEvent.ShowToast(R.string.applicant_list_toast_direct_chat_failed))
                    }
            }
        }

        private fun updateApplyStatus(
            applicantId: Long,
            accept: Boolean
        ) {
            viewModelScope.launch {
                repository
                    .updateApplyStatus(applicantId.toString(), accept)
                    .onSuccess { refresh() }
                    .onFailure {
                        val messageRes =
                            if (accept) {
                                R.string.applicant_list_toast_accept_failed
                            } else {
                                R.string.applicant_list_toast_reject_failed
                            }
                        _events.send(RecruitUiEvent.ShowToast(messageRes))
                    }
            }
        }

        private fun ApplicantEntry.toUiModel(): ApplicantUiModel =
            ApplicantUiModel(
                id = applyId.toLong(),
                memberId = memberId.toLong(),
                nickname = nickname,
                exp = exp,
                appliedDate = appliedAt.date.toDisplayString(),
                applicationMessage = message,
                keywords = keywords,
                status =
                    when (status) {
                        ApplyStatus.WAITING -> ApplicantStatus.PENDING
                        ApplyStatus.ACCEPTED -> ApplicantStatus.ACCEPTED
                        ApplyStatus.REJECTED -> ApplicantStatus.REJECTED
                    }
            )
    }
