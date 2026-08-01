package com.example.pickii.ui.recruitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.model.CurrentUser
import com.example.pickii.domain.model.RecruitComment
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.navigation.ARG_POST_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 댓글 전송을 위해 요구되는 최소 글자 수. */
private const val MIN_COMMENT_LENGTH = 2

/** 댓글로 입력 가능한 최대 글자 수. [RecruitDetailScreen]의 글자 수 카운터도 이 값을 그대로 사용한다. */
internal const val MAX_COMMENT_LENGTH = 100

/** 답글의 답글이라도 화면에서는 한 단계까지만 들여쓰기로 표시하기 위한 최대 시각적 깊이. */
private const val MAX_VISUAL_INDENT_LEVEL = 1

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

/** [RecruitDetailScreen]에 표시되는 상태. */
data class RecruitDetailUiState(
    val post: RecruitPost? = null,
    val comments: List<RecruitComment> = emptyList(),
    val isScraped: Boolean = false,
    val hasApplied: Boolean = false,
    val currentUser: CurrentUser? = null,
    val isLoggedIn: Boolean = false,
    val commentDraft: String = "",
    val replyTargetCommentId: String? = null,
    val replyTargetNickname: String? = null,
    val isLoginPromptVisible: Boolean = false,
    val pendingDeleteCommentId: String? = null,
    val isDeletePostDialogVisible: Boolean = false,
    val isClosePostDialogVisible: Boolean = false
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

/** 공고 상세 화면의 상태를 보관하고, 스크랩·지원·댓글·작성자 전용 동작을 처리한다. */
@HiltViewModel
class RecruitDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val recruitRepository: RecruitRepository,
        private val sessionRepository: SessionRepository
    ) : ViewModel() {
        private val postId: String = requireNotNull(savedStateHandle[ARG_POST_ID]) { "postId가 필요합니다." }

        private val _uiState = MutableStateFlow(RecruitDetailUiState())
        val uiState: StateFlow<RecruitDetailUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)

        /** 토스트 등 한 번만 소비해야 하는 이벤트. */
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        init {
            observeDetail()
        }

        /** 게시글, 댓글, 로그인 상태를 함께 구독해 화면 상태에 반영한다. */
        private fun observeDetail() {
            viewModelScope.launch {
                combine(
                    recruitRepository.observePosts(),
                    recruitRepository.observeComments(postId),
                    sessionRepository.isLoggedIn,
                    sessionRepository.currentUser
                ) { posts, comments, isLoggedIn, currentUser ->
                    DetailSnapshot(
                        post = posts.firstOrNull { it.id == postId },
                        comments = comments,
                        isLoggedIn = isLoggedIn,
                        currentUser = currentUser
                    )
                }.collect { snapshot -> applySnapshot(snapshot) }
            }
        }

        /** 구독 결과를 스크랩/지원 여부와 함께 화면 상태에 반영한다. */
        private fun applySnapshot(snapshot: DetailSnapshot) {
            val userId = snapshot.currentUser?.id
            val isScraped = userId?.let { recruitRepository.isScraped(postId, it) } ?: false
            val hasApplied = userId?.let { recruitRepository.hasApplied(postId, it) } ?: false
            _uiState.update {
                it.copy(
                    post = snapshot.post,
                    comments = snapshot.comments,
                    isLoggedIn = snapshot.isLoggedIn,
                    currentUser = snapshot.currentUser,
                    isScraped = isScraped,
                    hasApplied = hasApplied
                )
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

        /** 이미 지원한 게시글에 다시 지원을 시도했을 때 안내 토스트를 띄운다. */
        fun onAlreadyAppliedNoticeRequested() {
            emitEvent(RecruitUiEvent.ShowToast(R.string.recruit_detail_toast_already_applied))
        }

        /** 스크랩 상태를 토글한다. 비로그인 상태면 로그인 유도 팝업을 띄운다. */
        fun onScrapClick() {
            val userId = _uiState.value.currentUser?.id
            if (userId == null) {
                onLoginPromptRequested()
                return
            }
            viewModelScope.launch {
                recruitRepository.toggleScrap(postId, userId).onSuccess { isScraped ->
                    _uiState.update { it.copy(isScraped = isScraped) }
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

        /**
         * 댓글 또는 답글을 전송한다.
         *
         * 비로그인 상태면 로그인 유도 팝업을, 2자 미만이면 안내 토스트를 띄우고 전송하지 않는다.
         */
        fun onSubmitComment() {
            val state = _uiState.value
            val user = state.currentUser
            if (user == null) {
                onLoginPromptRequested()
                return
            }
            if (!state.isCommentDraftValid) {
                emitEvent(RecruitUiEvent.ShowToast(R.string.recruit_comment_toast_too_short))
                return
            }
            viewModelScope.launch {
                recruitRepository.addComment(
                    postId = postId,
                    authorId = user.id,
                    authorNickname = user.nickname,
                    parentCommentId = state.replyTargetCommentId,
                    content = state.commentDraft.trim()
                )
                _uiState.update { it.copy(commentDraft = "", replyTargetCommentId = null, replyTargetNickname = null) }
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
                recruitRepository.deleteComment(commentId)
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
                recruitRepository.closePost(postId)
                _uiState.update { it.copy(isClosePostDialogVisible = false) }
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
                recruitRepository.deletePost(postId)
                _uiState.update { it.copy(isDeletePostDialogVisible = false) }
                onDeleted()
            }
        }

        /** 1회성 이벤트를 발행한다. */
        private fun emitEvent(event: RecruitUiEvent) {
            viewModelScope.launch { _events.send(event) }
        }
    }
