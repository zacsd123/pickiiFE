package com.example.pickii.ui.recruitapply

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.domain.model.ApplyKeywordCategory
import com.example.pickii.domain.model.CurrentUser
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.ui.common.AiDialogState
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.navigation.ARG_POST_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** "전달 메시지"에 입력할 수 있는 최대 글자 수. */
private const val MAX_MESSAGE_LENGTH = 300

/** 서버가 이미 지원한 공고에 다시 지원했을 때 내려주는 에러 코드. */
private const val ERROR_CODE_ALREADY_APPLIED = "ALREADY_APPLIED"

/** 지원 키워드는 전체 카테고리를 통틀어 최대 이 개수까지만 선택할 수 있다(5-7 정책). */
internal const val MAX_KEYWORD_SELECTION = 5

/** [RecruitApplyScreen]에 표시되는 상태. */
data class RecruitApplyUiState(
    val post: RecruitPost? = null,
    val currentUser: CurrentUser? = null,
    val message: String = "",
    val availableKeywordCategories: List<ApplyKeywordCategory> = emptyList(),
    val selectedKeywordIds: Set<Long> = emptySet(),
    val aiDialogState: AiDialogState = AiDialogState.Hidden,
    val isSubmitConfirmDialogVisible: Boolean = false,
    val isCompletionDialogVisible: Boolean = false,
    val isBackWarningDialogVisible: Boolean = false
) {
    /** 지원 가능 인원(모집 인원 - 현재 지원 인원). 게시글을 아직 불러오지 못했으면 0이다. */
    val availableSlots: Int
        get() = (post?.maxParticipants ?: 0) - (post?.currentParticipants ?: 0)

    /** 현재 사용자가 프로필(Resume)을 보유하고 있는지 여부. */
    val hasProfile: Boolean
        get() = currentUser?.hasProfile == true
}

/**
 * 공고 지원 화면의 상태를 보관하고, [RecruitRepository]/[SessionRepository]를 통해
 * 지원 메시지 작성, AI 초안 생성, 지원 등록을 처리한다.
 */
@HiltViewModel
class RecruitApplyViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val recruitRepository: RecruitRepository,
        private val sessionRepository: SessionRepository,
        private val masterDataRepository: MasterDataRepository
    ) : ViewModel() {
        private val postId: String = requireNotNull(savedStateHandle.get<String>(ARG_POST_ID)) { "postId가 필요합니다." }

        private val _uiState = MutableStateFlow(RecruitApplyUiState())
        val uiState: StateFlow<RecruitApplyUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        init {
            loadPost()
            observeCurrentUser()
            loadApplyKeywords()
        }

        /** 지원 키워드 선택지를 카테고리별로 불러온다(5-7). */
        private fun loadApplyKeywords() {
            viewModelScope.launch {
                masterDataRepository.getApplyKeywords().onSuccess { categories ->
                    _uiState.update { it.copy(availableKeywordCategories = categories) }
                }
            }
        }

        /** 지원 키워드 칩을 토글한다. 이미 [MAX_KEYWORD_SELECTION]개를 선택한 상태에서는 새로 선택할 수 없다. */
        fun onKeywordToggle(keywordId: Long) {
            _uiState.update { state ->
                val selected = state.selectedKeywordIds
                val updated =
                    when {
                        keywordId in selected -> selected - keywordId
                        selected.size >= MAX_KEYWORD_SELECTION -> selected
                        else -> selected + keywordId
                    }
                state.copy(selectedKeywordIds = updated)
            }
        }

        /** 이 화면이 보여줄 게시글을 [RecruitRepository]에서 한 번 조회한다. */
        private fun loadPost() {
            viewModelScope.launch {
                recruitRepository.getPostById(postId).onSuccess { post ->
                    _uiState.update { it.copy(post = post) }
                }
            }
        }

        /** [SessionRepository]에서 현재 로그인한 사용자를 구독해 프로필 보유 여부 등을 반영한다. */
        private fun observeCurrentUser() {
            viewModelScope.launch {
                sessionRepository.currentUser.collect { user ->
                    _uiState.update { it.copy(currentUser = user) }
                }
            }
        }

        /** 전달 메시지를 변경한다. 최대 글자 수를 넘는 입력은 무시한다. */
        fun onMessageChange(value: String) {
            if (value.length <= MAX_MESSAGE_LENGTH) {
                _uiState.update { it.copy(message = value) }
            }
        }

        /** "AI 도움받기" 버튼을 클릭해 지원 메시지 초안 생성을 시작한다. */
        fun onAiHelpClick() {
            generateAiDraft()
        }

        /** AI 초안 생성 실패 팝업에서 "다시 시도"를 클릭한다. */
        fun onAiRetryClick() {
            generateAiDraft()
        }

        /** AI 초안 생성 실패 팝업을 닫는다. */
        fun onAiDialogDismiss() {
            _uiState.update { it.copy(aiDialogState = AiDialogState.Hidden) }
        }

        /**
         * 지금까지 입력한 메시지를 공고 맥락에 맞게 AI로 다듬는다.
         *
         * 원본 메시지가 필요한 API라, 비어 있으면 호출하지 않고 안내 토스트만 띄운다.
         */
        private fun generateAiDraft() {
            val message = _uiState.value.message
            if (message.isBlank()) {
                emitEvent(RecruitUiEvent.ShowToast(R.string.recruit_apply_toast_ai_draft_message_required))
                return
            }
            _uiState.update { it.copy(aiDialogState = AiDialogState.Loading) }
            viewModelScope.launch {
                recruitRepository
                    .generateApplyAiDraft(postId, message)
                    .onSuccess { draft ->
                        _uiState.update { it.copy(message = draft, aiDialogState = AiDialogState.Hidden) }
                    }.onFailure {
                        _uiState.update { it.copy(aiDialogState = AiDialogState.Failed) }
                    }
            }
        }

        /**
         * "지원하기" 버튼을 클릭한다. 모집이 마감된 공고면 토스트만 보여주고,
         * 그렇지 않으면 지원 확인 팝업을 연다.
         */
        fun onSubmitClick() {
            val post = _uiState.value.post ?: return
            if (post.status == RecruitStatus.CLOSED) {
                viewModelScope.launch {
                    _events.send(RecruitUiEvent.ShowToast(R.string.recruit_apply_toast_closed_recruiting))
                }
                return
            }
            if (_uiState.value.message.isBlank()) {
                emitEvent(RecruitUiEvent.ShowToast(R.string.recruit_apply_toast_ai_draft_message_required))
                return
            }
            _uiState.update { it.copy(isSubmitConfirmDialogVisible = true) }
        }

        /** 지원 확인 팝업에서 확인을 눌러 실제로 지원을 등록한다. 이미 지원한 공고면 안내 토스트만 띄운다. */
        fun onSubmitConfirm() {
            val post = _uiState.value.post ?: return
            _uiState.update { it.copy(isSubmitConfirmDialogVisible = false) }
            viewModelScope.launch {
                recruitRepository
                    .submitApplication(
                        postId = post.id,
                        message = _uiState.value.message,
                        keywordIds = _uiState.value.selectedKeywordIds.toList()
                    )
                    .onSuccess {
                        _uiState.update { it.copy(isCompletionDialogVisible = true) }
                    }.onFailure { error ->
                        if (error is ApiException && error.code == ERROR_CODE_ALREADY_APPLIED) {
                            emitEvent(RecruitUiEvent.ShowToast(R.string.recruit_detail_toast_already_applied))
                        }
                    }
            }
        }

        /** 지원 확인 팝업을 닫는다. */
        fun onSubmitDialogDismiss() {
            _uiState.update { it.copy(isSubmitConfirmDialogVisible = false) }
        }

        /** 뒤로가기를 시도한다. 작성 중인 내용이 저장되지 않는다는 경고 팝업을 먼저 보여준다. */
        fun onBackRequested() {
            _uiState.update { it.copy(isBackWarningDialogVisible = true) }
        }

        /** 뒤로가기 경고 팝업에서 확인을 눌러 실제로 화면을 나간다. */
        fun onBackWarningConfirm(onConfirmed: () -> Unit) {
            _uiState.update { it.copy(isBackWarningDialogVisible = false) }
            onConfirmed()
        }

        /** 뒤로가기 경고 팝업을 닫고 화면에 남는다. */
        fun onBackWarningDismiss() {
            _uiState.update { it.copy(isBackWarningDialogVisible = false) }
        }

        /** 1회성 이벤트를 발행한다. */
        private fun emitEvent(event: RecruitUiEvent) {
            viewModelScope.launch { _events.send(event) }
        }
    }
