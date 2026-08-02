package com.example.pickii.ui.mypage.feedback.projectlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.FeedbackProject
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.EmptyStateMessage
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.PaginationRow
import com.example.pickii.ui.common.StatusBadge
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight

/**
 * 상호평가/피드백 화면(4-11). 내가 참여한 종료 프로젝트별 평가 진행률을 보여준다.
 *
 * @param onProjectClick 프로젝트 카드 클릭 콜백(팀원별 평가 화면으로 이동)
 */
@Composable
fun FeedbackProjectListScreen(
    onBackClick: () -> Unit,
    onProjectClick: (projectId: String) -> Unit,
    viewModel: FeedbackProjectListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedbackProjectListScreenContent(
        uiState = uiState,
        visiblePageNumbers = viewModel.visiblePageNumbers,
        onBackClick = onBackClick,
        onProjectClick = onProjectClick,
        onPageClick = viewModel::onPageClick,
        onPreviousPage = viewModel::onPreviousPage,
        onNextPage = viewModel::onNextPage
    )
}

@Composable
private fun FeedbackProjectListScreenContent(
    uiState: FeedbackProjectListUiState,
    visiblePageNumbers: List<Int>,
    onBackClick: () -> Unit,
    onProjectClick: (String) -> Unit,
    onPageClick: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(PickiiYellowLight).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        BackHeader(title = stringResource(R.string.mypage_feedback_title), onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.items.isEmpty() -> EmptyStateMessage(stringResource(R.string.mypage_feedback_empty))
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    uiState.items.forEach { project ->
                        FeedbackProjectCard(project = project, onClick = { onProjectClick(project.projectId) })
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
}

@Composable
private fun FeedbackProjectCard(
    project: FeedbackProject,
    onClick: () -> Unit
) {
    val isDone = project.evaluatedCount >= project.requiredCount
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable(onClick = onClick)
                .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = project.name,
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(
                label =
                    stringResource(
                        if (isDone) R.string.mypage_feedback_status_done else R.string.mypage_feedback_status_pending
                    ),
                containerColor = if (isDone) PickiiBlue else PickiiDisabledGray,
                contentColor = if (isDone) Color.White else Color.Black
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "${project.periodStart} ~ ${project.periodEnd}", color = PickiiTextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.mypage_feedback_progress, project.evaluatedCount, project.memberCount),
            color = PickiiTextGray,
            fontSize = 12.sp
        )
    }
}
