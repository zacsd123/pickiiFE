package com.example.pickii.ui.recruitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.domain.model.CurrentUser
import com.example.pickii.domain.model.RecruitComment
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.navigation.ARG_POST_ID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 댓글 전송을 위해 요구되는 최소 글자 수. */
private const val MIN_COMMENT_LENGTH = 2

/** 댓글로 입력 가능한 최대 글자 수. [RecruitDetailScreen]의 글자 수 카운터도 이 값을 그대로 사용한다. */
internal const val MAX_COMMENT_LENGTH = 100

/** 답글의 답글이라도 화면에서는 한 단계까지만 들여쓰기로 표시하기 위한 최대 시각적 깊이. */
private const val MAX_VISUAL_INDENT_LEVEL = 1

/** 본인과 1:1 채팅을 시도했을 때 서버가 내려주는 에러 코드. */
private const val ERROR_CODE_CANNOT_CHAT_SELF = "CANNOT_CHAT_SELF"

/**
 * 댓글 목록을 화면에 그릴 순서(오래된 순, 답글은 부모 아래)로 펼친 뒤, 각 댓글의 들여쓰기 깊이를 계산한다.
 * 답글의 답글도 시간순으로는 올바른 위치에 배치되지만, 시각적 들여쓰기는 [MAX_VISUAL_INDENT_LEVEL]로 고정된다.
 */
private fun buildCommentDisplayList(comments: List<RecruitComment>): List<CommentDisplayItem> {
    val childrenByParentId = comments.filter { it.parentCommentId != null }.groupBy { it.parentCommentId }
    val roots = comments.filter { it.parentCommentId == null }.sortedBy { it.createdAt }
    val result = mutableListOf<CommentDisplayItem>()

    fun appendWithChildren(
        comment: RecruitComment,
        depth: Int
    ) {
        result.add(CommentDisplayItem(comment = comment, depth = depth))
        val children = childrenByParentId[comment.id].orEmpty().sortedBy { it.createdAt }
        children.forEach { child -> appendWithChildren(child, depth + 1) }
    }

    roots.forEach { root -> appendWithChildren(root, depth = 0) }
    return result
}

/** 댓글/답글 하나와, 화면에 그릴 때 사용할 들여쓰기 깊이. */
data class CommentDisplayItem(
    val comment: RecruitComment,
    val depth: Int
) {
    /** 실제 답글 깊이와 무관하게 화면에 적용할 들여쓰기 단계(최대 [MAX_VISUAL_INDENT_LEVEL]). */
    val visualIndentLevel: Int get() = depth.coerceAtMost(MAX_VISUAL_INDENT_LEVEL)
}

/** 댓글 작성자 닉네임을 눌렀을 때 뜨는 메뉴(프로필 보기/1:1 채팅)의 대상. */
data class CommentAuthorMenuTarget(
    val authorId: String,
    val authorNickname: String
)

/** [RecruitDetailScreen]에 표시되는 상태. */
data class RecruitDetailUiState(
    val post: RecruitPost? = null,
    val comments: List<RecruitComment> = emptyList(),
    val isScraped: Boolean = false,
    val currentUser: CurrentUser? = null,
    val isLoggedIn: Boolean = false,
    val commentDraft: String = "",
    val replyTargetCommentId: String? = null,
    val replyTargetNickname: String? = null,
    val isLoginPromptVisible: Boolean = false,
    val pendingDeleteCommentId: String? = null,
    val isDeletePostDialogVisible: Boolean = false,
    val isClosePostDialogVisible: Boolean = false,
    val isReopenPostDialogVisible: Boolean = false,
    val commentAuthorMenuTarget: CommentAuthorMenuTarget? = null
) {
    /** 댓글을 오래된 순으로 정렬하고 답글을 들여쓰기 깊이와 함께 펼친 목록. */
    val displayComments: List<CommentDisplayItem>
        get() = buildCommentDisplayList(comments)

    /** 댓글 입력값이 전송 가능한 길이인지 여부(공백만 입력한 경우는 제외). */
    val isCommentDraftValid: Boolean
        get() = commentDraft.trim().length >= MIN_COMMENT_LENGTH

    /** 현재 로그인한 사용자가 이 게시글의 작성자인지 여부. */
    val isAuthor: Boolean
        get() = currentUser != null && post != null && currentUser.id == post.authorId

    /** 답글 작성 모드인지 여부. */
    val isReplyMode: Boolean
        get() = replyTargetCommentId != null

    /** 댓글 삭제 확인 팝업 노출 여부. */
    val isDeleteCommentDialogVisible: Boolean
        get() = pendingDeleteCommentId != null
}

/** 게시글/댓글 조회 결과와 로그인 상태를 하나로 묶어 전달하기 위한 내부 스냅샷. */
private data class DetailSnapshot(
    val post: RecruitPost?,
    val comments: List<RecruitComment>,
    val isLoggedIn: Boolean,
    val currentUser: CurrentUser?
)

/**
 * 공고 상세 화면의 상태를 보관하고, [RecruitRepository]로 게시글/댓글을 조회하며 스크랩·지원·댓글·작성자 전용 동작을 처리한다.
 *
 * `observePosts()`처럼 전체를 구독하는 API가 서버에 없으므로, [postFlow]/[commentsFlow]에 조회 결과를
 * 직접 채워 넣고 성공한 변경 동작 뒤에는 해당 부분만 다시 조회한다.
 */
class RecruitDetailViewModel
    constructor(
        savedStateHandle: SavedStateHandle,
        private val recruitRepository: RecruitRepository,
        private val sessionRepository: SessionRepository,
        private val chatRepository: ChatRepository
    ) : ViewModel() {
        private val postId: String = requireNotNull(savedStateHandle[ARG_POST_ID]) { "postId가 필요합니다." }

        private val postFlow = MutableStateFlow<RecruitPost?>(null)
        private val commentsFlow = MutableStateFlow<List<RecruitComment>>(emptyList())

        private val _uiState = MutableStateFlow(RecruitDetailUiState())
        val uiState: StateFlow<RecruitDetailUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)

        /** 토스트 등 한 번만 소비해야 하는 이벤트. */
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        private val _navigateToChatRoom = Channel<Long>(Channel.BUFFERED)

        /** 댓글 작성자와 1:1 채팅방 개설이 성공했을 때 이동할 채팅방 id(1회성 이벤트). */
        val navigateToChatRoom: Flow<Long> = _navigateToChatRoom.receiveAsFlow()

        init {
            observeDetail()
            loadPost()
            loadComments()
        }

        /** 게시글, 댓글, 로그인 상태를 함께 구독해 화면 상태에 반영한다. */
        private fun observeDetail() {
            viewModelScope.launch {
                combine(
                    postFlow,
                    commentsFlow,
                    sessionRepository.isLoggedIn,
                    sessionRepository.currentUser
                ) { post, comments, isLoggedIn, currentUser ->
                    DetailSnapshot(
                        post = post,
                        comments = comments,
                        isLoggedIn = isLoggedIn,
                        currentUser = currentUser
                    )
                }.collect { snapshot ->
                    _uiState.update {
                        it.copy(
                            post = snapshot.post,
                            comments = snapshot.comments,
                            isScraped = snapshot.post?.isScrapped ?: false,
                            isLoggedIn = snapshot.isLoggedIn,
                            currentUser = snapshot.currentUser
                        )
                    }
                }
            }
        }

        /** 화면이 다시 보일 때(ON_RESUME) 최신 게시글 정보로 갱신한다(예: 수정 화면에서 돌아왔을 때). */
        fun refresh() {
            loadPost()
        }

        private fun loadPost() {
            viewModelScope.launch {
                recruitRepository.getPostById(postId).onSuccess { postFlow.value = it }
            }
        }

        private fun loadComments() {
            viewModelScope.launch {
                recruitRepository.getComments(postId).onSuccess { commentsFlow.value = it }
            }
        }

        /** 로그인이 필요한 동작을 시도했을 때 로그인 유도 팝업을 띄운다. */
        fun onLoginPromptRequested() {
            _uiState.update { it.copy(isLoginPromptVisible = true) }
        }

        /** 로그인 유도 팝업을 닫는다. */
        fun onDismissLoginPrompt() {
            _uiState.update { it.copy(isLoginPromptVisible = false) }
        }

        /** 스크랩 상태를 토글한다. 비로그인 상태면 로그인 유도 팝업을 띄운다. */
        fun onScrapClick() {
            if (!_uiState.value.isLoggedIn) {
                onLoginPromptRequested()
                return
            }
            val wasScraped = _uiState.value.isScraped
            viewModelScope.launch {
                if (wasScraped) {
                    recruitRepository.unscrapPost(postId).onSuccess {
                        postFlow.update { post -> post?.copy(isScrapped = false) }
                    }
                } else {
                    recruitRepository.scrapPost(postId).onSuccess { isScrapped ->
                        postFlow.update { post -> post?.copy(isScrapped = isScrapped) }
                    }
                }
            }
        }

        /** 댓글/답글 입력값을 갱신한다. 최대 글자 수를 넘는 입력은 무시한다. */
        fun onCommentDraftChange(value: String) {
            if (value.length <= MAX_COMMENT_LENGTH) {
                _uiState.update { it.copy(commentDraft = value) }
            }
        }

        /** 답글 작성 버튼 클릭 시 입력란을 "@닉네임에게 답글 작성 중" 모드로 전환한다. */
        fun onReplyClick(comment: RecruitComment) {
            _uiState.update {
                it.copy(
                    replyTargetCommentId = comment.id,
                    replyTargetNickname = comment.authorNickname,
                    commentDraft = ""
                )
            }
        }

        /** 답글 작성 모드를 취소하고 일반 댓글 모드로 되돌아간다. */
        fun onCancelReply() {
            _uiState.update { it.copy(replyTargetCommentId = null, replyTargetNickname = null) }
        }

        /** 댓글 작성자 닉네임 클릭 시 "프로필 보기/1:1 채팅" 메뉴를 띄운다. */
        fun onCommentAuthorClick(comment: RecruitComment) {
            _uiState.update {
                it.copy(
                    commentAuthorMenuTarget =
                        CommentAuthorMenuTarget(
                            authorId = comment.authorId,
                            authorNickname = comment.authorNickname
                        )
                )
            }
        }

        /** 댓글 작성자 메뉴를 닫는다. */
        fun onDismissCommentAuthorMenu() {
            _uiState.update { it.copy(commentAuthorMenuTarget = null) }
        }

        /** 댓글 작성자 메뉴에서 "1:1 채팅하기"를 눌러, 해당 작성자와의 채팅방을 개설(또는 조회)한다. */
        fun onCommentAuthorChatClick() {
            val authorId =
                _uiState.value.commentAuthorMenuTarget
                    ?.authorId
                    ?.toLongOrNull() ?: return
            _uiState.update { it.copy(commentAuthorMenuTarget = null) }
            viewModelScope.launch {
                chatRepository
                    .createDirectChatRoom(authorId)
                    .onSuccess { result -> _navigateToChatRoom.send(result.chatRoomId) }
                    .onFailure { error ->
                        val messageRes =
                            if (error is ApiException && error.code == ERROR_CODE_CANNOT_CHAT_SELF) {
                                R.string.chat_toast_cannot_chat_self
                            } else {
                                R.string.chat_toast_generic_error
                            }
                        emitEvent(RecruitUiEvent.ShowToast(messageRes))
                    }
            }
        }

        /**
         * 댓글 또는 답글을 전송한다.
         *
         * 비로그인 상태면 로그인 유도 팝업을, 2자 미만이면 안내 토스트를 띄우고 전송하지 않는다.
         */
        fun onSubmitComment() {
            val state = _uiState.value
            if (!state.isLoggedIn) {
                onLoginPromptRequested()
                return
            }
            if (!state.isCommentDraftValid) {
                emitEvent(RecruitUiEvent.ShowToast(R.string.recruit_comment_toast_too_short))
                return
            }
            viewModelScope.launch {
                recruitRepository
                    .addComment(
                        postId = postId,
                        parentCommentId = state.replyTargetCommentId,
                        content = state.commentDraft.trim()
                    ).onSuccess {
                        _uiState.update {
                            it.copy(commentDraft = "", replyTargetCommentId = null, replyTargetNickname = null)
                        }
                        loadComments()
                    }
            }
        }

        /** 댓글 삭제 확인 팝업을 띄운다. */
        fun onDeleteCommentRequested(commentId: String) {
            _uiState.update { it.copy(pendingDeleteCommentId = commentId) }
        }

        /** 댓글 삭제 확인 팝업을 닫는다. */
        fun onDismissDeleteCommentDialog() {
            _uiState.update { it.copy(pendingDeleteCommentId = null) }
        }

        /** 댓글 삭제를 확정한다(soft delete). */
        fun onConfirmDeleteComment() {
            val commentId = _uiState.value.pendingDeleteCommentId ?: return
            viewModelScope.launch {
                recruitRepository.deleteComment(commentId).onSuccess { loadComments() }
                _uiState.update { it.copy(pendingDeleteCommentId = null) }
            }
        }

        /** 공고 마감 확인 팝업을 띄운다. */
        fun onCloseRecruitingRequested() {
            _uiState.update { it.copy(isClosePostDialogVisible = true) }
        }

        /** 공고 마감 확인 팝업을 닫는다. */
        fun onDismissCloseRecruitingDialog() {
            _uiState.update { it.copy(isClosePostDialogVisible = false) }
        }

        /** 공고 마감을 확정한다. */
        fun onConfirmCloseRecruiting() {
            viewModelScope.launch {
                recruitRepository.closePost(postId).onSuccess { loadPost() }
                _uiState.update { it.copy(isClosePostDialogVisible = false) }
            }
        }

        /** 추가 모집 확인 팝업을 띄운다. */
        fun onReopenRecruitingRequested() {
            _uiState.update { it.copy(isReopenPostDialogVisible = true) }
        }

        /** 추가 모집 확인 팝업을 닫는다. */
        fun onDismissReopenRecruitingDialog() {
            _uiState.update { it.copy(isReopenPostDialogVisible = false) }
        }

        /** 추가 모집을 확정한다. */
        fun onConfirmReopenRecruiting() {
            viewModelScope.launch {
                recruitRepository.reopenAdditionalRecruiting(postId).onSuccess { loadPost() }
                _uiState.update { it.copy(isReopenPostDialogVisible = false) }
            }
        }

        /** 공고 삭제 확인 팝업을 띄운다. */
        fun onDeletePostRequested() {
            _uiState.update { it.copy(isDeletePostDialogVisible = true) }
        }

        /** 공고 삭제 확인 팝업을 닫는다. */
        fun onDismissDeletePostDialog() {
            _uiState.update { it.copy(isDeletePostDialogVisible = false) }
        }

        /**
         * 공고 삭제를 확정하고, 삭제가 끝나면 [onDeleted]를 호출한다.
         *
         * @param onDeleted 삭제 완료 후 호출되는 콜백(홈 화면 이동 등 내비게이션 용도)
         */
        fun onConfirmDeletePost(onDeleted: () -> Unit) {
            viewModelScope.launch {
                recruitRepository.deletePost(postId).onSuccess {
                    _uiState.update { it.copy(isDeletePostDialogVisible = false) }
                    onDeleted()
                }
            }
        }

        /** 1회성 이벤트를 발행한다. */
        private fun emitEvent(event: RecruitUiEvent) {
            viewModelScope.launch { _events.send(event) }
        }
    }
