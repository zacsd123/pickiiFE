package com.example.pickii.ui.recruitapply

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.domain.model.ApplyKeywordCategory
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_button_cancel
import com.example.pickii.shared.generated.resources.common_button_confirm
import com.example.pickii.shared.generated.resources.common_counter_format
import com.example.pickii.shared.generated.resources.ic_arrow_back
import com.example.pickii.shared.generated.resources.ic_notifications
import com.example.pickii.shared.generated.resources.recruit_apply_banner_notice
import com.example.pickii.shared.generated.resources.recruit_apply_banner_tip_subtitle
import com.example.pickii.shared.generated.resources.recruit_apply_banner_tip_title
import com.example.pickii.shared.generated.resources.recruit_apply_button_ai_help
import com.example.pickii.shared.generated.resources.recruit_apply_button_create_profile
import com.example.pickii.shared.generated.resources.recruit_apply_button_go_home
import com.example.pickii.shared.generated.resources.recruit_apply_button_submit
import com.example.pickii.shared.generated.resources.recruit_apply_button_view_application_status
import com.example.pickii.shared.generated.resources.recruit_apply_dialog_back_warning_body
import com.example.pickii.shared.generated.resources.recruit_apply_dialog_back_warning_title
import com.example.pickii.shared.generated.resources.recruit_apply_dialog_complete_title
import com.example.pickii.shared.generated.resources.recruit_apply_dialog_submit_body
import com.example.pickii.shared.generated.resources.recruit_apply_dialog_submit_title
import com.example.pickii.shared.generated.resources.recruit_apply_label_keywords
import com.example.pickii.shared.generated.resources.recruit_apply_label_message
import com.example.pickii.shared.generated.resources.recruit_apply_label_no_profile_guidance
import com.example.pickii.shared.generated.resources.recruit_apply_placeholder_message
import com.example.pickii.ui.common.AiDialogState
import com.example.pickii.ui.common.AiGenerationDialog
import com.example.pickii.ui.common.CharacterCounterText
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.LocalSnackbarHostState
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.common.StatusBadge
import com.example.pickii.ui.common.recruitStatusColor
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellow
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/** "전달 메시지"에 입력할 수 있는 최대 글자 수(화면 표시용, [RecruitApplyViewModel]과 동일한 값). */
private const val MAX_MESSAGE_LENGTH = 300

/** 카드/버튼에 공통으로 사용하는 모서리 둥글기. */
private val CardCornerRadius = 16.dp

/** 지원 가능 인원을 나타내는 점의 지름. */
private val SlotDotSize = 12.dp

/**
 * 공고 지원 화면.
 *
 * [RecruitApplyViewModel]에서 게시글 정보와 입력 상태를 받아와 [RecruitApplyScreenContent]에 전달한다.
 *
 * @param onBackClick 뒤로가기 경고 팝업에서 확인했을 때 호출되는 콜백
 * @param onGoHomeClick 지원 완료 팝업에서 "홈으로 이동" 클릭 콜백
 * @param onViewApplicationStatusClick 지원 완료 팝업에서 "지원 현황" 클릭 콜백
 * @param onCreateProfileClick 프로필이 없을 때 "프로필 만들러 가기" 클릭 콜백
 * @param onNavigateToLogin 로그인 화면으로 이동하는 콜백(현재 이 화면에서는 사용하지 않는다. 상세 화면에서 이미
 * 로그인 여부를 확인한 뒤에만 진입하므로, 예비로 시그니처만 맞춰 둔다)
 */
@Composable
fun RecruitApplyScreen(
    onBackClick: () -> Unit = {},
    onGoHomeClick: () -> Unit = {},
    onViewApplicationStatusClick: () -> Unit = {},
    onCreateProfileClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: RecruitApplyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                snackbarHostState.showSnackbar(getString(event.messageRes))
        }
    }

    BackHandler(onBack = viewModel::onBackRequested)

    RecruitApplyScreenContent(
        uiState = uiState,
        onMessageChange = viewModel::onMessageChange,
        onKeywordToggle = viewModel::onKeywordToggle,
        onAiHelpClick = viewModel::onAiHelpClick,
        onAiRetryClick = viewModel::onAiRetryClick,
        onAiDialogDismiss = viewModel::onAiDialogDismiss,
        onSubmitClick = viewModel::onSubmitClick,
        onSubmitConfirm = viewModel::onSubmitConfirm,
        onSubmitDialogDismiss = viewModel::onSubmitDialogDismiss,
        onBackIconClick = viewModel::onBackRequested,
        onBackWarningConfirm = { viewModel.onBackWarningConfirm(onConfirmed = onBackClick) },
        onBackWarningDismiss = viewModel::onBackWarningDismiss,
        onGoHomeClick = onGoHomeClick,
        onViewApplicationStatusClick = onViewApplicationStatusClick,
        onCreateProfileClick = onCreateProfileClick
    )
}

/** [RecruitApplyScreen]의 실제 UI. ViewModel 없이도 미리보기가 가능하도록 상태를 파라미터로 받는다. */
@Composable
private fun RecruitApplyScreenContent(
    uiState: RecruitApplyUiState,
    onMessageChange: (String) -> Unit,
    onKeywordToggle: (Long) -> Unit,
    onAiHelpClick: () -> Unit,
    onAiRetryClick: () -> Unit,
    onAiDialogDismiss: () -> Unit,
    onSubmitClick: () -> Unit,
    onSubmitConfirm: () -> Unit,
    onSubmitDialogDismiss: () -> Unit,
    onBackIconClick: () -> Unit,
    onBackWarningConfirm: () -> Unit,
    onBackWarningDismiss: () -> Unit,
    onGoHomeClick: () -> Unit,
    onViewApplicationStatusClick: () -> Unit,
    onCreateProfileClick: () -> Unit
) {
    val post = uiState.post

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecruitApplyTopBar(onBackClick = onBackIconClick)

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
            ) {
                if (post != null) {
                    PostSummaryCard(post = post, availableSlots = uiState.availableSlots)
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!uiState.hasProfile) {
                    NoProfileGuidance(onCreateProfileClick = onCreateProfileClick)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                NoticeBanner(text = stringResource(Res.string.recruit_apply_banner_notice))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.recruit_apply_label_message),
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                MessageField(value = uiState.message, onValueChange = onMessageChange)

                Spacer(modifier = Modifier.height(4.dp))

                CharacterCounterText(
                    current = uiState.message.length,
                    max = MAX_MESSAGE_LENGTH,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.availableKeywordCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    ApplyKeywordSection(
                        categories = uiState.availableKeywordCategories,
                        selectedKeywordIds = uiState.selectedKeywordIds,
                        onKeywordToggle = onKeywordToggle
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AiHelpBanner(onAiHelpClick = onAiHelpClick)

                Spacer(modifier = Modifier.height(24.dp))

                SubmitButton(onClick = onSubmitClick)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (uiState.aiDialogState != AiDialogState.Hidden) {
            AiGenerationDialog(state = uiState.aiDialogState, onRetry = onAiRetryClick, onDismiss = onAiDialogDismiss)
        }

        if (uiState.isSubmitConfirmDialogVisible) {
            ConfirmDialog(
                title = stringResource(Res.string.recruit_apply_dialog_submit_title),
                body = stringResource(Res.string.recruit_apply_dialog_submit_body),
                confirmLabel = stringResource(Res.string.common_button_confirm),
                dismissLabel = stringResource(Res.string.common_button_cancel),
                onConfirm = onSubmitConfirm,
                onDismiss = onSubmitDialogDismiss
            )
        }

        if (uiState.isCompletionDialogVisible) {
            ApplyCompletionDialog(
                onGoHomeClick = onGoHomeClick,
                onViewApplicationStatusClick = onViewApplicationStatusClick
            )
        }

        if (uiState.isBackWarningDialogVisible) {
            ConfirmDialog(
                title = stringResource(Res.string.recruit_apply_dialog_back_warning_title),
                body = stringResource(Res.string.recruit_apply_dialog_back_warning_body),
                confirmLabel = stringResource(Res.string.common_button_confirm),
                dismissLabel = stringResource(Res.string.common_button_cancel),
                onConfirm = onBackWarningConfirm,
                onDismiss = onBackWarningDismiss
            )
        }
    }
}

/** 뒤로가기 버튼, Pickii 로고, 알림 아이콘이 있는 상단 바. */
@Composable
private fun RecruitApplyTopBar(onBackClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_back),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.clickable(onClick = onBackClick)
        )

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

        Spacer(modifier = Modifier.width(6.dp))

        Text(text = "Pickii", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.weight(1f))

        Icon(painter = painterResource(Res.drawable.ic_notifications), contentDescription = null, tint = Color.Black)

        Spacer(modifier = Modifier.width(8.dp))
    }
}

/** 게시글 제목, 카테고리/주제, 지원 가능 인원을 보여주는 요약 카드. */
@Composable
private fun PostSummaryCard(
    post: RecruitPost,
    availableSlots: Int
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(PickiiFieldBackground)
                .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = post.title,
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(
                label = post.status.label,
                containerColor = recruitStatusColor(post.status),
                contentColor = if (post.status == RecruitStatus.CLOSED) Color.Black else Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            post.categories.forEach { InfoChip(label = it.label) }
            post.topics.forEach { InfoChip(label = it.label) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AvailableSlotsIndicator(
            current = post.currentParticipants,
            max = post.maxParticipants,
            availableSlots = availableSlots
        )
    }
}

/** 정보 표시 전용(클릭 불가) 회색 칩. */
@Composable
private fun InfoChip(label: String) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = PickiiTextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** 현재 지원 인원만큼 채워진 점과 남은 자리만큼 빈 점으로 지원 가능 인원을 보여준다. */
@Composable
private fun AvailableSlotsIndicator(
    current: Int,
    max: Int,
    availableSlots: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(current.coerceIn(0, max)) {
            Box(
                modifier =
                    Modifier
                        .size(SlotDotSize)
                        .clip(CircleShape)
                        .background(Color.Black)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        repeat(availableSlots.coerceAtLeast(0)) {
            Box(
                modifier =
                    Modifier
                        .size(SlotDotSize)
                        .clip(CircleShape)
                        .border(1.dp, PickiiTextGray, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = stringResource(Res.string.common_counter_format, current, max),
            color = Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 프로필이 없는 사용자에게 프로필 생성을 안내하는 배너. */
@Composable
private fun NoProfileGuidance(onCreateProfileClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(PickiiFieldBackground)
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.recruit_apply_label_no_profile_guidance),
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black)
                    .clickable(onClick = onCreateProfileClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.recruit_apply_button_create_profile),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** 노란 배경의 안내 문구 배너("작성한 메시지는 ~에게 전달됩니다" 등). */
@Composable
private fun NoticeBanner(text: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PickiiYellow.copy(alpha = 0.3f))
                .padding(12.dp)
    ) {
        Text(text = text, color = Color.Black, fontSize = 12.sp)
    }
}

/** "전달 메시지"를 입력하는 여러 줄 텍스트 필드. */
@Composable
private fun MessageField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(160.dp),
        placeholder = {
            Text(text = stringResource(Res.string.recruit_apply_placeholder_message), color = PickiiTextGray)
        },
        shape = RoundedCornerShape(12.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PickiiFieldBackground,
                unfocusedContainerColor = PickiiFieldBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
    )
}

/**
 * 지원 키워드를 카테고리별로 보여주고 선택/해제할 수 있는 영역. 전체 카테고리를 통틀어
 * 최대 [MAX_KEYWORD_SELECTION]개까지 선택 가능하다(5-7 정책).
 */
@Composable
private fun ApplyKeywordSection(
    categories: List<ApplyKeywordCategory>,
    selectedKeywordIds: Set<Long>,
    onKeywordToggle: (Long) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.recruit_apply_label_keywords),
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${selectedKeywordIds.size}/$MAX_KEYWORD_SELECTION",
                color = PickiiTextGray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.forEach { category ->
                Column {
                    Text(text = category.category, color = PickiiTextGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        category.keywords.forEach { keyword ->
                            KeywordChip(
                                label = keyword.content,
                                selected = keyword.keywordId in selectedKeywordIds,
                                onClick = { onKeywordToggle(keyword.keywordId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 선택 시 검은 배경, 미선택 시 흰 배경으로 바뀌는 지원 키워드 칩. */
@Composable
private fun KeywordChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selected) Color.Black else PickiiFieldBackground)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** "AI로부터 도움받기"를 안내하는 배너. */
@Composable
private fun AiHelpBanner(onAiHelpClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(PickiiYellow)
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.recruit_apply_banner_tip_title),
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.recruit_apply_banner_tip_subtitle),
                color = PickiiTextGray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black)
                    .clickable(onClick = onAiHelpClick)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(Res.string.recruit_apply_button_ai_help),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** "지원하기" 제출 버튼. */
@Composable
private fun SubmitButton(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(PickiiBlue)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.recruit_apply_button_submit),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** 지원 완료 후 "홈으로 이동"/"지원 현황" 중 하나를 고르는 팝업. */
@Composable
private fun ApplyCompletionDialog(
    onGoHomeClick: () -> Unit,
    onViewApplicationStatusClick: () -> Unit
) {
    ConfirmDialog(
        title = stringResource(Res.string.recruit_apply_dialog_complete_title),
        confirmLabel = stringResource(Res.string.recruit_apply_button_view_application_status),
        onConfirm = onViewApplicationStatusClick,
        dismissLabel = stringResource(Res.string.recruit_apply_button_go_home),
        onDismiss = onGoHomeClick
    )
}

/** [RecruitApplyScreen]의 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun RecruitApplyScreenPreview() {
    MaterialTheme {
        RecruitApplyScreenContent(
            uiState = RecruitApplyUiState(),
            onMessageChange = {},
            onKeywordToggle = {},
            onAiHelpClick = {},
            onAiRetryClick = {},
            onAiDialogDismiss = {},
            onSubmitClick = {},
            onSubmitConfirm = {},
            onSubmitDialogDismiss = {},
            onBackIconClick = {},
            onBackWarningConfirm = {},
            onBackWarningDismiss = {},
            onGoHomeClick = {},
            onViewApplicationStatusClick = {},
            onCreateProfileClick = {}
        )
    }
}
