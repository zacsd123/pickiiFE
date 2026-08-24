package com.example.pickii.ui.recruitform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.recruit_form_toast_ai_draft_content_required
import com.example.pickii.shared.generated.resources.recruit_form_toast_edit_complete
import com.example.pickii.ui.common.AiDialogState
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.navigation.ARG_POST_ID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** 공고 등록/수정 폼이 어느 모드로 동작 중인지. */
enum class RecruitFormMode {
    CREATE,
    EDIT
}

/** 공고 제목의 최소 글자 수. */
internal const val MIN_TITLE_LENGTH = 2

/** 공고 제목의 최대 글자 수. */
internal const val MAX_TITLE_LENGTH = 20

/** 모집 인원의 최소값. */
internal const val MIN_PARTICIPANTS = 1

/** 모집 인원의 최대값. */
internal const val MAX_PARTICIPANTS = 7

/** 간단 소개의 최대 글자 수. */
internal const val MAX_SHORT_INTRO_LENGTH = 50

/** 상세 사항의 최대 글자 수. */
internal const val MAX_DETAIL_LENGTH = 1000

/** [RecruitFormScreen]에 표시되는 입력 상태. */
data class RecruitFormUiState(
    val mode: RecruitFormMode = RecruitFormMode.CREATE,
    val isLoginRequired: Boolean = false,
    val isAuthorized: Boolean = true,
    val availableCategories: List<RecruitCategory> = emptyList(),
    val availableTopics: List<RecruitTopic> = emptyList(),
    val title: String = "",
    val maxParticipants: Int? = null,
    val onCampus: CampusScope? = CampusScope.INTERNAL,
    val categories: Set<RecruitCategory> = emptySet(),
    val topics: Set<RecruitTopic> = emptySet(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val shortIntro: String = "",
    val detailContent: String = "",
    val aiDialogState: AiDialogState = AiDialogState.Hidden,
    val isSubmitConfirmDialogVisible: Boolean = false,
    val isCompleteDialogVisible: Boolean = false
) {
    /** 제목이 최소/최대 글자 수를 만족하는지 여부. */
    val isTitleValid: Boolean
        get() = title.length in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH

    /** 필수 항목(제목, 모집 인원, 교내/교외, 카테고리, 주제, 진행 기간, 간단 소개, 상세 사항)을 모두 채웠는지 여부. */
    val isSubmitEnabled: Boolean
        get() =
            isTitleValid &&
                maxParticipants != null &&
                onCampus != null &&
                categories.isNotEmpty() &&
                topics.isNotEmpty() &&
                startDate != null &&
                endDate != null &&
                shortIntro.isNotBlank() &&
                detailContent.isNotBlank()
}

/**
 * 공고 글 등록/수정 폼의 입력 상태를 보관하고, [RecruitRepository]를 통해 저장한다.
 *
 * 내비게이션 인자로 전달된 [ARG_POST_ID]가 없으면 등록([RecruitFormMode.CREATE]), 있으면 수정([RecruitFormMode.EDIT]) 모드로 동작한다.
 */
class RecruitFormViewModel
    constructor(
        savedStateHandle: SavedStateHandle,
        private val recruitRepository: RecruitRepository,
        private val masterDataRepository: MasterDataRepository,
        private val sessionRepository: SessionRepository
    ) : ViewModel() {
        private val postId: String? = savedStateHandle[ARG_POST_ID]

        private val _uiState =
            MutableStateFlow(
                RecruitFormUiState(mode = if (postId == null) RecruitFormMode.CREATE else RecruitFormMode.EDIT)
            )
        val uiState: StateFlow<RecruitFormUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        init {
            loadFilterOptions()
            val editTargetPostId = postId
            if (editTargetPostId == null) {
                checkLoginForCreate()
            } else {
                loadExistingPost(editTargetPostId)
            }
        }

        /** 카테고리/주제 선택지를 마스터 데이터에서 불러온다. */
        private fun loadFilterOptions() {
            viewModelScope.launch {
                masterDataRepository.getCategories().onSuccess { categories ->
                    _uiState.update { it.copy(availableCategories = categories) }
                }
            }
            viewModelScope.launch {
                masterDataRepository.getTopics().onSuccess { topics ->
                    _uiState.update { it.copy(availableTopics = topics) }
                }
            }
        }

        /** 등록 화면 진입 시 로그인 상태를 확인해, 비로그인이면 로그인 유도 팝업을 띄운다. */
        private fun checkLoginForCreate() {
            _uiState.update { it.copy(isLoginRequired = !sessionRepository.isLoggedIn.value) }
        }

        /** 수정 대상 글을 불러와 폼에 채우고, 현재 사용자가 작성자인지 확인한다. 작성자가 아니면 폼 대신 권한 없음 상태로 전환한다. */
        private fun loadExistingPost(postId: String) {
            viewModelScope.launch {
                val post = recruitRepository.getPostById(postId).getOrNull()
                val currentUserId = sessionRepository.currentUser.value?.id
                if (post == null || currentUserId == null || post.authorId != currentUserId) {
                    _uiState.update { it.copy(isAuthorized = false) }
                    return@launch
                }

                applyExistingPost(post)
            }
        }

        private fun applyExistingPost(post: RecruitPost) {
            _uiState.update {
                it.copy(
                    isAuthorized = true,
                    title = post.title,
                    maxParticipants = post.maxParticipants,
                    onCampus = post.onCampus,
                    categories = post.categories.toSet(),
                    topics = post.topics.toSet(),
                    startDate = post.startDate,
                    endDate = post.endDate,
                    shortIntro = post.shortIntro,
                    detailContent = post.detailContent
                )
            }
        }

        /** 공고 제목을 바꾼다. 최대 글자 수를 넘는 입력은 무시한다. */
        fun onTitleChange(value: String) {
            if (value.length <= MAX_TITLE_LENGTH) {
                _uiState.update { it.copy(title = value) }
            }
        }

        /** 모집 인원을 바꾼다. 허용 범위를 벗어나면 가까운 경계값으로 보정한다. */
        fun onMaxParticipantsChange(value: Int) {
            _uiState.update { it.copy(maxParticipants = value.coerceIn(MIN_PARTICIPANTS, MAX_PARTICIPANTS)) }
        }

        /** 교내/교외 범위를 바꾼다. */
        fun onCampusScopeChange(scope: CampusScope) {
            _uiState.update { it.copy(onCampus = scope) }
        }

        /** 카테고리 선택을 토글한다(복수 선택 가능). */
        fun onCategorySelect(category: RecruitCategory) {
            _uiState.update {
                val selected = it.categories
                it.copy(categories = if (category in selected) selected - category else selected + category)
            }
        }

        /** 주제 선택을 토글한다(복수 선택 가능). */
        fun onTopicSelect(topic: RecruitTopic) {
            _uiState.update {
                val selected = it.topics
                it.copy(topics = if (topic in selected) selected - topic else selected + topic)
            }
        }

        /** 진행 기간의 시작일을 바꾼다. */
        fun onStartDateChange(date: LocalDate) {
            _uiState.update { it.copy(startDate = date) }
        }

        /** 진행 기간의 종료일을 바꾼다. */
        fun onEndDateChange(date: LocalDate) {
            _uiState.update { it.copy(endDate = date) }
        }

        /** 간단 소개를 바꾼다. 최대 글자 수를 넘는 입력은 무시한다. */
        fun onShortIntroChange(value: String) {
            if (value.length <= MAX_SHORT_INTRO_LENGTH) {
                _uiState.update { it.copy(shortIntro = value) }
            }
        }

        /** 상세 사항을 바꾼다. 최대 글자 수를 넘는 입력은 무시한다. */
        fun onDetailContentChange(value: String) {
            if (value.length <= MAX_DETAIL_LENGTH) {
                _uiState.update { it.copy(detailContent = value) }
            }
        }

        /**
         * "AI 초안 생성" 버튼 클릭 시, 입력해 둔 간단 소개/상세 사항을 AI가 다듬어 채운다.
         *
         * 상세 사항은 서버에서 필수 입력이라, 비어 있으면 API를 호출하지 않고 안내 토스트만 띄운다.
         */
        fun onGenerateAiDraftClick() {
            val state = _uiState.value
            if (state.detailContent.isBlank()) {
                emitEvent(RecruitUiEvent.ShowToast(Res.string.recruit_form_toast_ai_draft_content_required))
                return
            }

            _uiState.update { it.copy(aiDialogState = AiDialogState.Loading) }
            viewModelScope.launch {
                recruitRepository
                    .generateAiDraft(simpleDesc = state.shortIntro.ifBlank { null }, content = state.detailContent)
                    .onSuccess { (simpleDesc, content) ->
                        _uiState.update {
                            it.copy(
                                shortIntro = simpleDesc.take(MAX_SHORT_INTRO_LENGTH),
                                detailContent = content.take(MAX_DETAIL_LENGTH),
                                aiDialogState = AiDialogState.Hidden
                            )
                        }
                    }.onFailure {
                        _uiState.update { it.copy(aiDialogState = AiDialogState.Failed) }
                    }
            }
        }

        /** AI 초안 생성 실패 팝업을 닫는다. */
        fun onDismissAiDialog() {
            _uiState.update { it.copy(aiDialogState = AiDialogState.Hidden) }
        }

        /** 등록/수정 완료 버튼 클릭 시 확인 팝업을 띄운다. */
        fun onSubmitClick() {
            _uiState.update { it.copy(isSubmitConfirmDialogVisible = true) }
        }

        /** 등록/수정 확인 팝업을 취소한다. */
        fun onDismissSubmitConfirm() {
            _uiState.update { it.copy(isSubmitConfirmDialogVisible = false) }
        }

        /**
         * 등록/수정을 확정한다. 수정 모드는 저장에 성공하면 바로 [onEditComplete]를 호출하고,
         * 등록 모드는 완료 팝업을 띄운 뒤 사용자가 확인을 눌러야 [onEditComplete]에 해당하는 콜백이 호출된다.
         *
         * @param onEditComplete 수정 모드에서 저장 성공 직후 호출되는 콜백(화면 전환 용도)
         */
        fun onConfirmSubmit(onEditComplete: () -> Unit) {
            _uiState.update { it.copy(isSubmitConfirmDialogVisible = false) }
            when (_uiState.value.mode) {
                RecruitFormMode.CREATE -> createPost()
                RecruitFormMode.EDIT -> updatePost(onEditComplete)
            }
        }

        /** 완료 팝업의 확인 버튼 클릭 시 팝업을 닫고 [onComplete]를 호출한다(등록 모드 전용). */
        fun onCompleteDialogConfirm(onComplete: () -> Unit) {
            _uiState.update { it.copy(isCompleteDialogVisible = false) }
            onComplete()
        }

        /** 새 모집 글을 등록한다. */
        private fun createPost() {
            val state = _uiState.value
            val maxParticipants = state.maxParticipants ?: return
            val onCampus = state.onCampus ?: return
            if (state.categories.isEmpty() || state.topics.isEmpty()) return
            val startDate = state.startDate ?: return
            val endDate = state.endDate ?: return

            viewModelScope.launch {
                recruitRepository
                    .createPost(
                        title = state.title,
                        maxParticipants = maxParticipants,
                        onCampus = onCampus,
                        categoryIds = state.categories.map { it.id },
                        topicIds = state.topics.map { it.id },
                        startDate = startDate,
                        endDate = endDate,
                        shortIntro = state.shortIntro,
                        detailContent = state.detailContent
                    ).onSuccess {
                        _uiState.update { it.copy(isCompleteDialogVisible = true) }
                    }
            }
        }

        /** 기존 모집 글의 사용자 입력 항목만 바꿔서 저장한다. */
        private fun updatePost(onComplete: () -> Unit) {
            val state = _uiState.value
            val targetPostId = postId ?: return
            val maxParticipants = state.maxParticipants ?: return
            val onCampus = state.onCampus ?: return
            if (state.categories.isEmpty() || state.topics.isEmpty()) return
            val startDate = state.startDate ?: return
            val endDate = state.endDate ?: return

            viewModelScope.launch {
                recruitRepository
                    .updatePost(
                        postId = targetPostId,
                        title = state.title,
                        maxParticipants = maxParticipants,
                        onCampus = onCampus,
                        categoryIds = state.categories.map { it.id },
                        topicIds = state.topics.map { it.id },
                        startDate = startDate,
                        endDate = endDate,
                        shortIntro = state.shortIntro,
                        detailContent = state.detailContent
                    ).onSuccess {
                        _events.send(RecruitUiEvent.ShowToast(Res.string.recruit_form_toast_edit_complete))
                        onComplete()
                    }
            }
        }

        /** 1회성 이벤트를 발행한다. */
        private fun emitEvent(event: RecruitUiEvent) {
            viewModelScope.launch { _events.send(event) }
        }
    }
