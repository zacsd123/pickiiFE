package com.example.pickii.ui.recruitdetail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.domain.model.RecruitComment
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_button_cancel
import com.example.pickii.shared.generated.resources.common_button_confirm
import com.example.pickii.shared.generated.resources.recruit_comment_author_menu_chat
import com.example.pickii.shared.generated.resources.recruit_comment_author_menu_profile
import com.example.pickii.shared.generated.resources.recruit_comment_button_cancel_reply
import com.example.pickii.shared.generated.resources.recruit_comment_button_delete
import com.example.pickii.shared.generated.resources.recruit_comment_button_reply
import com.example.pickii.shared.generated.resources.recruit_comment_deleted_placeholder
import com.example.pickii.shared.generated.resources.recruit_comment_dialog_delete_body
import com.example.pickii.shared.generated.resources.recruit_comment_dialog_delete_title
import com.example.pickii.shared.generated.resources.recruit_comment_empty_state
import com.example.pickii.shared.generated.resources.recruit_comment_header_title
import com.example.pickii.shared.generated.resources.recruit_comment_placeholder_input
import com.example.pickii.shared.generated.resources.recruit_comment_reply_indent_prefix
import com.example.pickii.shared.generated.resources.recruit_comment_reply_mode_prefix
import com.example.pickii.shared.generated.resources.recruit_detail_button_apply
import com.example.pickii.shared.generated.resources.recruit_detail_button_close_recruiting
import com.example.pickii.shared.generated.resources.recruit_detail_button_delete_post
import com.example.pickii.shared.generated.resources.recruit_detail_button_edit
import com.example.pickii.shared.generated.resources.recruit_detail_button_reopen_recruiting
import com.example.pickii.shared.generated.resources.recruit_detail_dialog_close_post_body
import com.example.pickii.shared.generated.resources.recruit_detail_dialog_close_post_title
import com.example.pickii.shared.generated.resources.recruit_detail_dialog_delete_post_body
import com.example.pickii.shared.generated.resources.recruit_detail_dialog_delete_post_title
import com.example.pickii.shared.generated.resources.recruit_detail_dialog_reopen_post_body
import com.example.pickii.shared.generated.resources.recruit_detail_dialog_reopen_post_title
import com.example.pickii.shared.generated.resources.recruit_detail_label_author
import com.example.pickii.shared.generated.resources.recruit_detail_label_author_experience
import com.example.pickii.shared.generated.resources.recruit_detail_label_detail_content
import com.example.pickii.shared.generated.resources.recruit_detail_label_period_and_capacity
import com.example.pickii.shared.generated.resources.recruit_detail_label_short_intro
import com.example.pickii.shared.generated.resources.recruit_detail_label_written_at
import com.example.pickii.shared.generated.resources.unknown_author_nickname
import com.example.pickii.ui.common.CharacterCounterText
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.LevelAvatar
import com.example.pickii.ui.common.LoginRequiredDialog
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiPaletteRed
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.util.calculateLevel
import com.example.pickii.util.toDisplayString
import com.example.pickii.util.toFullDisplayString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/** 카드/버튼에 공통으로 사용하는 모서리 둥글기. */
private val CardCornerRadius = 20.dp

/** 지원하기 버튼의 높이. */
private val ApplyButtonHeight = 52.dp

/** 작성자 전용 버튼 한 칸의 높이. */
private val AuthorActionButtonHeight = 44.dp

/** 답글 한 단계당 들여쓰는 폭. */
private val CommentIndentStep = 20.dp

/** 작성자 아바타 자리의 크기. */
private val AvatarSize = 40.dp

/**
 * 공고 상세 화면.
 *
 * [RecruitDetailViewModel]에서 게시글/댓글 상태를 받아와 [RecruitDetailScreenContent]에 전달한다.
 *
 * @param onBackClick 뒤로 가기 클릭 콜백
 * @param onAuthorProfileClick 작성자 닉네임 클릭 콜백(작성자 id 전달)
 * @param onApplyClick 지원 페이지 이동 콜백(게시글 id 전달, 로그인/중복지원 검증을 통과했을 때만 호출)
 * @param onEditClick 공고 수정 화면 이동 콜백(게시글 id 전달)
 * @param onDeletedNavigateHome 공고 삭제 완료 후 홈으로 이동하는 콜백
 * @param onNavigateToLogin 로그인 유도 팝업에서 "로그인하러 가기"를 눌렀을 때 호출되는 콜백
 */
@Composable
fun RecruitDetailScreen(
    onBackClick: () -> Unit = {},
    onAuthorProfileClick: (authorId: String) -> Unit = {},
    onApplyClick: (postId: String) -> Unit = {},
    onEditClick: (postId: String) -> Unit = {},
    onDeletedNavigateHome: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToChatRoom: (roomId: Long) -> Unit = {},
    viewModel: RecruitDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refresh()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                Toast
                    .makeText(
                        context,
                        getString(event.messageRes),
                        Toast.LENGTH_SHORT
                    ).show()
        }
    }

    OneShotEventEffect(flow = viewModel.navigateToChatRoom) { roomId ->
        onNavigateToChatRoom(roomId)
    }

    RecruitDetailScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onScrapClick = viewModel::onScrapClick,
        onAuthorClick = { uiState.post?.let { onAuthorProfileClick(it.authorId) } },
        onCommentAuthorClick = viewModel::onCommentAuthorClick,
        onDismissCommentAuthorMenu = viewModel::onDismissCommentAuthorMenu,
        onCommentAuthorProfileClick = onAuthorProfileClick,
        onCommentAuthorChatClick = viewModel::onCommentAuthorChatClick,
        onApplyClick = {
            when {
                !uiState.isLoggedIn -> viewModel.onLoginPromptRequested()
                else -> uiState.post?.let { onApplyClick(it.id) }
            }
        },
        onEditClick = { uiState.post?.let { onEditClick(it.id) } },
        onCloseRecruitingClick = viewModel::onCloseRecruitingRequested,
        onReopenRecruitingClick = viewModel::onReopenRecruitingRequested,
        onDeletePostClick = viewModel::onDeletePostRequested,
        onCommentDraftChange = viewModel::onCommentDraftChange,
        onReplyClick = viewModel::onReplyClick,
        onCancelReply = viewModel::onCancelReply,
        onSubmitComment = viewModel::onSubmitComment,
        onDeleteCommentClick = viewModel::onDeleteCommentRequested,
        onDismissLoginPrompt = viewModel::onDismissLoginPrompt,
        onLoginPromptConfirm = onNavigateToLogin,
        onDismissDeleteCommentDialog = viewModel::onDismissDeleteCommentDialog,
        onConfirmDeleteComment = viewModel::onConfirmDeleteComment,
        onDismissCloseRecruitingDialog = viewModel::onDismissCloseRecruitingDialog,
        onConfirmCloseRecruiting = viewModel::onConfirmCloseRecruiting,
        onDismissReopenRecruitingDialog = viewModel::onDismissReopenRecruitingDialog,
        onConfirmReopenRecruiting = viewModel::onConfirmReopenRecruiting,
        onDismissDeletePostDialog = viewModel::onDismissDeletePostDialog,
        onConfirmDeletePost = { viewModel.onConfirmDeletePost(onDeleted = onDeletedNavigateHome) }
    )
}

/** [RecruitDetailScreen]의 실제 UI. ViewModel 없이도 미리보기가 가능하도록 상태를 파라미터로 받는다. */
@Composable
private fun RecruitDetailScreenContent(
    uiState: RecruitDetailUiState,
    onBackClick: () -> Unit,
    onScrapClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onCommentAuthorClick: (RecruitComment) -> Unit,
    onDismissCommentAuthorMenu: () -> Unit,
    onCommentAuthorProfileClick: (authorId: String) -> Unit,
    onCommentAuthorChatClick: () -> Unit,
    onApplyClick: () -> Unit,
    onEditClick: () -> Unit,
    onCloseRecruitingClick: () -> Unit,
    onReopenRecruitingClick: () -> Unit,
    onDeletePostClick: () -> Unit,
    onCommentDraftChange: (String) -> Unit,
    onReplyClick: (RecruitComment) -> Unit,
    onCancelReply: () -> Unit,
    onSubmitComment: () -> Unit,
    onDeleteCommentClick: (String) -> Unit,
    onDismissLoginPrompt: () -> Unit,
    onLoginPromptConfirm: () -> Unit,
    onDismissDeleteCommentDialog: () -> Unit,
    onConfirmDeleteComment: () -> Unit,
    onDismissCloseRecruitingDialog: () -> Unit,
    onConfirmCloseRecruiting: () -> Unit,
    onDismissReopenRecruitingDialog: () -> Unit,
    onConfirmReopenRecruiting: () -> Unit,
    onDismissDeletePostDialog: () -> Unit,
    onConfirmDeletePost: () -> Unit
) {
    val post = uiState.post

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                CommentInputBar(
                    draft = uiState.commentDraft,
                    isReplyMode = uiState.isReplyMode,
                    replyTargetNickname = uiState.replyTargetNickname,
                    onDraftChange = onCommentDraftChange,
                    onCancelReply = onCancelReply,
                    onSubmit = onSubmitComment
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item { DetailTopBar(onBackClick = onBackClick) }

                if (post != null) {
                    item {
                        PostInfoCard(
                            post = post,
                            isScraped = uiState.isScraped,
                            isAuthor = uiState.isAuthor,
                            onScrapClick = onScrapClick,
                            onAuthorClick = onAuthorClick,
                            onApplyClick = onApplyClick,
                            onEditClick = onEditClick,
                            onCloseRecruitingClick = onCloseRecruitingClick,
                            onReopenRecruitingClick = onReopenRecruitingClick,
                            onDeletePostClick = onDeletePostClick
                        )
                    }
                }

                item { CommentsHeader() }

                if (uiState.displayComments.isEmpty()) {
                    item { EmptyCommentsText() }
                } else {
                    items(uiState.displayComments, key = { it.comment.id }) { item ->
                        CommentRow(
                            item = item,
                            currentUserId = uiState.currentUser?.id,
                            onReplyClick = onReplyClick,
                            onDeleteClick = onDeleteCommentClick,
                            onAuthorClick = onCommentAuthorClick
                        )
                    }
                }
            }
        }

        if (uiState.isLoginPromptVisible) {
            LoginRequiredDialog(onLoginClick = onLoginPromptConfirm, onDismiss = onDismissLoginPrompt)
        }

        if (uiState.isDeleteCommentDialogVisible) {
            ConfirmDialog(
                title = stringResource(Res.string.recruit_comment_dialog_delete_title),
                body = stringResource(Res.string.recruit_comment_dialog_delete_body),
                confirmLabel = stringResource(Res.string.common_button_confirm),
                dismissLabel = stringResource(Res.string.common_button_cancel),
                onConfirm = onConfirmDeleteComment,
                onDismiss = onDismissDeleteCommentDialog
            )
        }

        if (uiState.isClosePostDialogVisible) {
            ConfirmDialog(
                title = stringResource(Res.string.recruit_detail_dialog_close_post_title),
                body = stringResource(Res.string.recruit_detail_dialog_close_post_body),
                confirmLabel = stringResource(Res.string.common_button_confirm),
                dismissLabel = stringResource(Res.string.common_button_cancel),
                onConfirm = onConfirmCloseRecruiting,
                onDismiss = onDismissCloseRecruitingDialog
            )
        }

        if (uiState.isReopenPostDialogVisible) {
            ConfirmDialog(
                title = stringResource(Res.string.recruit_detail_dialog_reopen_post_title),
                body = stringResource(Res.string.recruit_detail_dialog_reopen_post_body),
                confirmLabel = stringResource(Res.string.common_button_confirm),
                dismissLabel = stringResource(Res.string.common_button_cancel),
                onConfirm = onConfirmReopenRecruiting,
                onDismiss = onDismissReopenRecruitingDialog
            )
        }

        if (uiState.isDeletePostDialogVisible) {
            ConfirmDialog(
                title = stringResource(Res.string.recruit_detail_dialog_delete_post_title),
                body = stringResource(Res.string.recruit_detail_dialog_delete_post_body),
                confirmLabel = stringResource(Res.string.common_button_confirm),
                dismissLabel = stringResource(Res.string.common_button_cancel),
                onConfirm = onConfirmDeletePost,
                onDismiss = onDismissDeletePostDialog
            )
        }

        uiState.commentAuthorMenuTarget?.let { target ->
            CommentAuthorMenuBottomSheet(
                nickname = target.authorNickname,
                onProfileClick = {
                    onDismissCommentAuthorMenu()
                    onCommentAuthorProfileClick(target.authorId)
                },
                onChatClick = onCommentAuthorChatClick,
                onDismiss = onDismissCommentAuthorMenu
            )
        }
    }
}

/** 댓글 작성자 닉네임을 눌렀을 때 뜨는 "프로필 보기 / 1:1 채팅하기" 메뉴. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentAuthorMenuBottomSheet(
    nickname: String,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = nickname,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CommentAuthorMenuRow(
                icon = Icons.Filled.Person,
                label = stringResource(Res.string.recruit_comment_author_menu_profile),
                onClick = onProfileClick
            )

            CommentAuthorMenuRow(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = stringResource(Res.string.recruit_comment_author_menu_chat),
                onClick = onChatClick
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CommentAuthorMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PickiiFieldBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(text = label, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

/** 뒤로 가기 버튼과 Pickii 브랜딩이 있는 상단 바. */
@Composable
private fun DetailTopBar(onBackClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.clickable(onClick = onBackClick)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "P", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Pickii",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

/** 작은 정보 배지(교내/교외, 카테고리, 주제 표시용). */
@Composable
private fun InfoBadge(text: String) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(PickiiFieldBackground)
                .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = PickiiTextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** 게시글 정보, 스크랩, 지원, 작성자 전용 버튼을 보여주는 카드. */
@Composable
private fun PostInfoCard(
    post: RecruitPost,
    isScraped: Boolean,
    isAuthor: Boolean,
    onScrapClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onApplyClick: () -> Unit,
    onEditClick: () -> Unit,
    onCloseRecruitingClick: () -> Unit,
    onReopenRecruitingClick: () -> Unit,
    onDeletePostClick: () -> Unit
) {
    val isClosed = post.status == RecruitStatus.CLOSED

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(Color.White)
                .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelAvatar(exp = if (post.isAuthorUnknown) 0 else post.authorExperience, size = AvatarSize)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.recruit_detail_label_author),
                    color = PickiiTextGray,
                    fontSize = 12.sp
                )
                Text(
                    text =
                        stringResource(
                            Res.string.recruit_detail_label_author_experience,
                            if (post.isAuthorUnknown) {
                                stringResource(Res.string.unknown_author_nickname)
                            } else {
                                post.authorNickname
                            },
                            if (post.isAuthorUnknown) 0 else calculateLevel(post.authorExperience).level
                        ),
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !post.isAuthorUnknown, onClick = onAuthorClick)
                )
                Text(
                    text =
                        stringResource(
                            Res.string.recruit_detail_label_written_at,
                            post.createdAt.toFullDisplayString()
                        ),
                    color = PickiiTextGray,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onScrapClick) {
                Icon(
                    imageVector = if (isScraped) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isScraped) PickiiBlue else PickiiTextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = post.title, color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InfoBadge(text = post.onCampus.label)
            post.categories.forEach { InfoBadge(text = it.label) }
            post.topics.forEach { InfoBadge(text = it.label) }
            InfoBadge(text = post.status.label)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text =
                stringResource(
                    Res.string.recruit_detail_label_period_and_capacity,
                    post.startDate.toDisplayString(),
                    post.endDate.toDisplayString(),
                    (post.maxParticipants - post.currentParticipants).coerceAtLeast(0)
                ),
            color = PickiiBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoSection(title = stringResource(Res.string.recruit_detail_label_short_intro), body = post.shortIntro)

        Spacer(modifier = Modifier.height(12.dp))

        InfoSection(title = stringResource(Res.string.recruit_detail_label_detail_content), body = post.detailContent)

        Spacer(modifier = Modifier.height(20.dp))

        if (isAuthor) {
            AuthorActionButtons(
                isClosed = post.status == RecruitStatus.CLOSED,
                onEditClick = onEditClick,
                onCloseRecruitingClick = onCloseRecruitingClick,
                onReopenRecruitingClick = onReopenRecruitingClick,
                onDeletePostClick = onDeletePostClick
            )
        } else {
            val isClosed = post.status == RecruitStatus.CLOSED

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ApplyButtonHeight)
                        .clip(RoundedCornerShape(CardCornerRadius))
                        .background(
                            if (isClosed) PickiiFieldBackground else PickiiBlue
                        ).then(
                            if (!isClosed) {
                                Modifier.clickable(onClick = onApplyClick)
                            } else {
                                Modifier
                            }
                        ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.recruit_detail_button_apply),
                    color = if (isClosed) PickiiTextGray else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** 간단 소개, 상세 내용처럼 옅은 배경 위에 라벨과 본문을 보여주는 섹션. */
@Composable
private fun InfoSection(
    title: String,
    body: String
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PickiiFieldBackground)
                .padding(14.dp)
    ) {
        Text(text = title, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = body, color = Color.Black, fontSize = 13.sp)
    }
}

/**
 * 글 작성자에게만 노출되는 버튼 2개(수정하기 + 마감하기/추가모집하기). 마감된 공고는 추가모집하기를,
 * 그 외(모집중/추가모집중)에는 마감하기를 보여준다.
 */
@Composable
private fun AuthorActionButtons(
    isClosed: Boolean,
    onEditClick: () -> Unit,
    onCloseRecruitingClick: () -> Unit,
    onReopenRecruitingClick: () -> Unit,
    onDeletePostClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AuthorActionButton(
                text = stringResource(Res.string.recruit_detail_button_edit),
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            )
            if (isClosed) {
                AuthorActionButton(
                    text = stringResource(Res.string.recruit_detail_button_reopen_recruiting),
                    onClick = onReopenRecruitingClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                AuthorActionButton(
                    text = stringResource(Res.string.recruit_detail_button_close_recruiting),
                    onClick = onCloseRecruitingClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        AuthorActionButton(
            text = stringResource(Res.string.recruit_detail_button_delete_post),
            onClick = onDeletePostClick,
            contentColor = PickiiPaletteRed,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 작성자 전용 버튼 하나의 공통 스타일. */
@Composable
private fun AuthorActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black
) {
    Box(
        modifier =
            modifier
                .height(AuthorActionButtonHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(PickiiFieldBackground)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** 댓글 영역의 "댓글" 헤더. */
@Composable
private fun CommentsHeader() {
    Text(
        text = stringResource(Res.string.recruit_comment_header_title),
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

/** 댓글이 하나도 없을 때 보여주는 안내 문구. */
@Composable
private fun EmptyCommentsText() {
    Text(
        text = stringResource(Res.string.recruit_comment_empty_state),
        color = PickiiTextGray,
        fontSize = 13.sp,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
    )
}

/** 댓글 또는 답글 한 줄. 삭제된 댓글은 안내 문구만 보여주고 작성자/시각/삭제 버튼을 숨긴다. */
@Composable
private fun CommentRow(
    item: CommentDisplayItem,
    currentUserId: String?,
    onReplyClick: (RecruitComment) -> Unit,
    onDeleteClick: (String) -> Unit,
    onAuthorClick: (RecruitComment) -> Unit
) {
    val comment = item.comment
    // comment.isAuthor는 "이 댓글의 작성자가 공고 작성자인지"를 뜻하는 서버 값(작성자 배지용)이지,
    // "내가 쓴 댓글인지"가 아니다. 삭제 버튼 노출은 authorId를 currentUserId와 직접 비교해야 한다.
    val isOwnComment = !comment.isDeleted && currentUserId != null && comment.authorId == currentUserId
    val nicknameText =
        if (item.visualIndentLevel > 0) {
            stringResource(Res.string.recruit_comment_reply_indent_prefix, comment.authorNickname)
        } else {
            comment.authorNickname
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp + CommentIndentStep * item.visualIndentLevel, end = 16.dp, bottom = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PickiiFieldBackground)
                .padding(12.dp)
    ) {
        if (comment.isDeleted) {
            Text(
                text = stringResource(Res.string.recruit_comment_deleted_placeholder),
                color = PickiiTextGray,
                fontSize = 13.sp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nicknameText,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onAuthorClick(comment) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = comment.createdAt.date.toDisplayString(), color = PickiiTextGray, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = comment.content, color = Color.Black, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = stringResource(Res.string.recruit_comment_button_reply),
                    color = PickiiBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onReplyClick(comment) }
                )

                if (isOwnComment) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(Res.string.recruit_comment_button_delete),
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onDeleteClick(comment.id) }
                    )
                }
            }
        }
    }
}

/** 화면 하단에 고정되는 댓글 입력 영역. 답글 모드일 때는 대상 안내와 취소 버튼이 함께 표시된다. */
@Composable
private fun CommentInputBar(
    draft: String,
    isReplyMode: Boolean,
    replyTargetNickname: String?,
    onDraftChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (isReplyMode && replyTargetNickname != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.recruit_comment_reply_mode_prefix, replyTargetNickname),
                    color = PickiiBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onCancelReply) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.recruit_comment_button_cancel_reply),
                        tint = PickiiTextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.recruit_comment_placeholder_input),
                        color = PickiiTextGray,
                        fontSize = 13.sp
                    )
                },
                shape = RoundedCornerShape(CardCornerRadius),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PickiiFieldBackground,
                        unfocusedContainerColor = PickiiFieldBackground,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PickiiBlue)
                        .clickable(onClick = onSubmit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            CharacterCounterText(current = draft.length, max = MAX_COMMENT_LENGTH)
        }
    }
}

/** [RecruitDetailScreen]의 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun RecruitDetailScreenPreview() {
    MaterialTheme {
        RecruitDetailScreenContent(
            uiState = RecruitDetailUiState(),
            onBackClick = {},
            onScrapClick = {},
            onAuthorClick = {},
            onCommentAuthorClick = {},
            onDismissCommentAuthorMenu = {},
            onCommentAuthorProfileClick = {},
            onCommentAuthorChatClick = {},
            onApplyClick = {},
            onEditClick = {},
            onCloseRecruitingClick = {},
            onReopenRecruitingClick = {},
            onDeletePostClick = {},
            onCommentDraftChange = {},
            onReplyClick = {},
            onCancelReply = {},
            onSubmitComment = {},
            onDeleteCommentClick = {},
            onDismissLoginPrompt = {},
            onLoginPromptConfirm = {},
            onDismissDeleteCommentDialog = {},
            onConfirmDeleteComment = {},
            onDismissCloseRecruitingDialog = {},
            onConfirmCloseRecruiting = {},
            onDismissReopenRecruitingDialog = {},
            onConfirmReopenRecruiting = {},
            onDismissDeletePostDialog = {},
            onConfirmDeletePost = {}
        )
    }
}
