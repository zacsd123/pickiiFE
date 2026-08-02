package com.example.pickii.ui.mypage.myrecruits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.MyRecruitSummary
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.EmptyStateMessage
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.PaginationRow
import com.example.pickii.ui.common.StatusBadge
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import com.example.pickii.util.toFullDisplayString

/**
 * 작성 공고 화면(17번). 내가 등록한 공고 목록과 지원자조회/수정/마감/추가모집/삭제 액션.
 *
 * @param onNavigateToApplicantList "지원자 조회" 클릭 콜백
 * @param onNavigateToRecruitEdit "공고 수정" 클릭 콜백
 */
@Composable
fun MyRecruitsScreen(
    onBackClick: () -> Unit,
    onNavigateToApplicantList: (postId: String) -> Unit,
    onNavigateToRecruitEdit: (postId: String) -> Unit,
    viewModel: MyRecruitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyRecruitsScreenContent(
        uiState = uiState,
        visiblePageNumbers = viewModel.visiblePageNumbers,
        onBackClick = onBackClick,
        onApplicantsClick = onNavigateToApplicantList,
        onEditClick = onNavigateToRecruitEdit,
        onActionRequest = viewModel::onActionRequest,
        onDismissActionDialog = viewModel::onDismissActionDialog,
        onConfirmAction = viewModel::onConfirmAction,
        onPageClick = viewModel::onPageClick,
        onPreviousPage = viewModel::onPreviousPage,
        onNextPage = viewModel::onNextPage
    )
}

@Suppress("LongParameterList")
@Composable
private fun MyRecruitsScreenContent(
    uiState: MyRecruitsUiState,
    visiblePageNumbers: List<Int>,
    onBackClick: () -> Unit,
    onApplicantsClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onActionRequest: (String, MyRecruitPendingAction) -> Unit,
    onDismissActionDialog: () -> Unit,
    onConfirmAction: () -> Unit,
    onPageClick: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(PickiiYellowLight).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        BackHeader(title = stringResource(R.string.mypage_my_recruits_title), onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.items.isEmpty() -> EmptyStateMessage(stringResource(R.string.mypage_my_recruits_empty))
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    uiState.items.forEach { recruit ->
                        MyRecruitCard(
                            recruit = recruit,
                            onApplicantsClick = { onApplicantsClick(recruit.id) },
                            onEditClick = { onEditClick(recruit.id) },
                            onCloseClick = { onActionRequest(recruit.id, MyRecruitPendingAction.CLOSE) },
                            onReopenClick = { onActionRequest(recruit.id, MyRecruitPendingAction.REOPEN) },
                            onDeleteClick = { onActionRequest(recruit.id, MyRecruitPendingAction.DELETE) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PaginationRow(
                        currentPage = uiState.currentPage,
                        totalPages = uiState.totalPages,
                        visiblePageNumbers = visiblePageNumbers,
                        onPageClick = onPageClick,
                        onPreviousClick = onPreviousPage,
                        onNextClick = onNextPage
                    )
                }
            }
        }
    }

    if (uiState.pendingAction != null) {
        val (title, body) =
            when (uiState.pendingAction) {
                MyRecruitPendingAction.CLOSE ->
                    stringResource(R.string.mypage_my_recruits_close_confirm_title) to ""
                MyRecruitPendingAction.REOPEN ->
                    stringResource(R.string.mypage_my_recruits_reopen_confirm_title) to ""
                MyRecruitPendingAction.DELETE ->
                    stringResource(R.string.mypage_my_recruits_delete_confirm_title) to
                        stringResource(R.string.mypage_my_recruits_delete_confirm_body)
            }
        ConfirmDialog(
            title = title,
            body = body,
            confirmLabel = stringResource(R.string.common_button_confirm),
            dismissLabel = stringResource(R.string.common_button_cancel),
            onConfirm = onConfirmAction,
            onDismiss = onDismissActionDialog
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun MyRecruitCard(
    recruit: MyRecruitSummary,
    onApplicantsClick: () -> Unit,
    onEditClick: () -> Unit,
    onCloseClick: () -> Unit,
    onReopenClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isClosed = recruit.status == RecruitStatus.CLOSED
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .alpha(if (isClosed) 0.5f else 1f)
                .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = recruit.title,
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(
                label = recruit.status.label,
                containerColor = recruitStatusColor(recruit.status),
                contentColor = if (recruit.status == RecruitStatus.CLOSED) Color.Black else Color.White
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.mypage_my_recruits_label_date, recruit.createdAt.toFullDisplayString()),
            color = PickiiTextGray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardActionButton(stringResource(R.string.mypage_my_recruits_button_applicants), onApplicantsClick)
            if (recruit.status != RecruitStatus.CLOSED) {
                CardActionButton(stringResource(R.string.mypage_my_recruits_button_edit), onEditClick)
            }
            if (recruit.status == RecruitStatus.CLOSED) {
                CardActionButton(
                    stringResource(R.string.mypage_my_recruits_button_reopen),
                    onReopenClick,
                    isPrimary = true
                )
            } else {
                CardActionButton(
                    stringResource(R.string.mypage_my_recruits_button_close),
                    onCloseClick,
                    isDanger = true
                )
            }
            CardActionButton(stringResource(R.string.mypage_my_recruits_button_delete), onDeleteClick)
        }
    }
}

@Composable
private fun CardActionButton(
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isDanger: Boolean = false
) {
    val background =
        when {
            isPrimary -> PickiiBlue
            isDanger -> Color(0xFFE57373)
            else -> PickiiFieldBackground
        }
    val contentColor = if (isPrimary || isDanger) Color.White else Color.Black
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(background)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text = label, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

private fun recruitStatusColor(status: RecruitStatus): Color =
    when (status) {
        RecruitStatus.OPEN -> PickiiBlue
        RecruitStatus.CLOSED -> PickiiDisabledGray
        RecruitStatus.ADDITIONAL -> Color(0xFF66BB6A)
    }
