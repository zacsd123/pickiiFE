package com.example.pickii.ui.mypage.feedback.memberlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.FeedbackTeamMember
import com.example.pickii.ui.common.AvatarPlaceholder
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.EmptyStateMessage
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.StatusBadge
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiYellowLight

/**
 * 프로젝트 팀원별 상호평가 여부 화면(4-9).
 *
 * 평가 작성/AI 피드백 상세는 백엔드 필드가 명세서에만 있고 이번 범위에는 포함하지 않았다(완료/미완료 상태 표시까지만).
 */
@Composable
fun FeedbackMemberListScreen(
    projectId: String,
    onBackClick: () -> Unit,
    viewModel: FeedbackMemberListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.load(projectId)
    }

    FeedbackMemberListScreenContent(uiState = uiState, onBackClick = onBackClick)
}

@Composable
private fun FeedbackMemberListScreenContent(
    uiState: FeedbackMemberListUiState,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(PickiiYellowLight).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        BackHeader(title = stringResource(R.string.mypage_feedback_members_title), onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.members.isEmpty() -> EmptyStateMessage(stringResource(R.string.mypage_feedback_empty))
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    uiState.members.forEach { member ->
                        MemberRow(member)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(member: FeedbackTeamMember) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarPlaceholder(size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = member.nickname,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        StatusBadge(
            label =
                stringResource(
                    if (member.isEvaluated) R.string.mypage_feedback_status_done else R.string.mypage_feedback_status_pending
                ),
            containerColor = if (member.isEvaluated) PickiiBlue else PickiiDisabledGray,
            contentColor = if (member.isEvaluated) Color.White else Color.Black
        )
    }
}
