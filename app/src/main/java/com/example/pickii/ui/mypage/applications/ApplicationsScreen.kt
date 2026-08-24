package com.example.pickii.ui.mypage.applications

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.domain.model.ApplyStatus
import com.example.pickii.domain.model.MyApply
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_button_cancel
import com.example.pickii.shared.generated.resources.common_button_confirm
import com.example.pickii.shared.generated.resources.mypage_applications_button_cancel
import com.example.pickii.shared.generated.resources.mypage_applications_button_chat
import com.example.pickii.shared.generated.resources.mypage_applications_cancel_confirm_body
import com.example.pickii.shared.generated.resources.mypage_applications_cancel_confirm_title
import com.example.pickii.shared.generated.resources.mypage_applications_empty
import com.example.pickii.shared.generated.resources.mypage_applications_label_date
import com.example.pickii.shared.generated.resources.mypage_applications_title
import com.example.pickii.shared.generated.resources.mypage_apply_status_accepted
import com.example.pickii.shared.generated.resources.mypage_apply_status_rejected
import com.example.pickii.shared.generated.resources.mypage_apply_status_waiting
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.EmptyStateMessage
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.MyPageSectionHeader
import com.example.pickii.ui.common.PaginationRow
import com.example.pickii.ui.common.StatusBadge
import com.example.pickii.ui.theme.PickiiPaletteBaseWhite
import com.example.pickii.ui.theme.PickiiPaletteGray
import com.example.pickii.ui.theme.PickiiPaletteGreen
import com.example.pickii.ui.theme.PickiiPaletteRed
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.util.toFullDisplayString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/**
 * 지원 현황 화면(16번). 내가 지원한 공고 목록, WAITING 상태만 취소 가능, ACCEPTED+채팅방 있으면 바로가기.
 *
 * @param onNavigateToChatRoom "채팅방 바로가기" 클릭 콜백
 */
@Composable
fun ApplicationsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToChatRoom: (roomId: String) -> Unit,
    viewModel: ApplicationsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.toastMessageRes != null) {
        val messageRes = uiState.toastMessageRes
        LaunchedEffect(messageRes) {
            if (messageRes != null) Toast.makeText(context, getString(messageRes), Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    ApplicationsScreenContent(
        uiState = uiState,
        visiblePageNumbers = viewModel.visiblePageNumbers,
        onBackClick = onBackClick,
        onNotificationClick = onNotificationClick,
        onCancelRequest = viewModel::onCancelRequest,
        onDismissCancelDialog = viewModel::onDismissCancelDialog,
        onConfirmCancel = viewModel::onConfirmCancel,
        onNavigateToChatRoom = onNavigateToChatRoom,
        onPageClick = viewModel::onPageClick,
        onPreviousPage = viewModel::onPreviousPage,
        onNextPage = viewModel::onNextPage
    )
}

@Suppress("LongParameterList")
@Composable
private fun ApplicationsScreenContent(
    uiState: ApplicationsUiState,
    visiblePageNumbers: List<Int>,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCancelRequest: (String) -> Unit,
    onDismissCancelDialog: () -> Unit,
    onConfirmCancel: () -> Unit,
    onNavigateToChatRoom: (String) -> Unit,
    onPageClick: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(PickiiPaletteBaseWhite).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        MyPageSectionHeader(
            title = stringResource(Res.string.mypage_applications_title),
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.items.isEmpty() -> EmptyStateMessage(stringResource(Res.string.mypage_applications_empty))
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    uiState.items.forEach { apply ->
                        ApplyCard(
                            apply = apply,
                            onCancelClick = { onCancelRequest(apply.id) },
                            onChatClick = onNavigateToChatRoom
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

    if (uiState.pendingCancelApplyId != null) {
        ConfirmDialog(
            title = stringResource(Res.string.mypage_applications_cancel_confirm_title),
            body = stringResource(Res.string.mypage_applications_cancel_confirm_body),
            confirmLabel = stringResource(Res.string.common_button_confirm),
            dismissLabel = stringResource(Res.string.common_button_cancel),
            onConfirm = onConfirmCancel,
            onDismiss = onDismissCancelDialog
        )
    }
}

@Composable
private fun ApplyCard(
    apply: MyApply,
    onCancelClick: () -> Unit,
    onChatClick: (String) -> Unit
) {
    val isRejected = apply.status == ApplyStatus.REJECTED
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .alpha(if (isRejected) 0.5f else 1f)
                .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = apply.recruitTitle,
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(
                label = applyStatusLabel(apply.status),
                containerColor = applyStatusColor(apply.status),
                contentColor = if (apply.status == ApplyStatus.WAITING) Color.Black else Color.White
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.mypage_applications_label_date, apply.appliedAt.toFullDisplayString()),
            color = PickiiTextGray,
            fontSize = 12.sp
        )

        if (apply.status == ApplyStatus.WAITING || (apply.status == ApplyStatus.ACCEPTED && apply.chatRoomId != null)) {
            Spacer(modifier = Modifier.height(12.dp))
            if (apply.status == ApplyStatus.WAITING) {
                CardActionButton(
                    label = stringResource(Res.string.mypage_applications_button_cancel),
                    onClick = onCancelClick
                )
            } else {
                apply.chatRoomId?.let { chatRoomId ->
                    CardActionButton(
                        label = stringResource(Res.string.mypage_applications_button_chat),
                        onClick = { onChatClick(chatRoomId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardActionButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun applyStatusLabel(status: ApplyStatus): String =
    when (status) {
        ApplyStatus.WAITING -> stringResource(Res.string.mypage_apply_status_waiting)
        ApplyStatus.ACCEPTED -> stringResource(Res.string.mypage_apply_status_accepted)
        ApplyStatus.REJECTED -> stringResource(Res.string.mypage_apply_status_rejected)
    }

private fun applyStatusColor(status: ApplyStatus): Color =
    when (status) {
        ApplyStatus.WAITING -> PickiiPaletteGray
        ApplyStatus.ACCEPTED -> PickiiPaletteGreen
        ApplyStatus.REJECTED -> PickiiPaletteRed
    }
