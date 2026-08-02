package com.example.pickii.ui.mypage.mycomments

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.MyComment
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.ui.common.BackHeader
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
 * 작성한 댓글 화면(17번). 내가 쓴 댓글 목록과 "공고 글 바로가기".
 *
 * @param onNavigateToRecruitDetail "공고 글 바로가기" 클릭 콜백
 */
@Composable
fun MyCommentsScreen(
    onBackClick: () -> Unit,
    onNavigateToRecruitDetail: (postId: String) -> Unit,
    viewModel: MyCommentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyCommentsScreenContent(
        uiState = uiState,
        visiblePageNumbers = viewModel.visiblePageNumbers,
        onBackClick = onBackClick,
        onGoToPostClick = onNavigateToRecruitDetail,
        onPageClick = viewModel::onPageClick,
        onPreviousPage = viewModel::onPreviousPage,
        onNextPage = viewModel::onNextPage
    )
}

@Composable
private fun MyCommentsScreenContent(
    uiState: MyCommentsUiState,
    visiblePageNumbers: List<Int>,
    onBackClick: () -> Unit,
    onGoToPostClick: (String) -> Unit,
    onPageClick: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(PickiiYellowLight).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        BackHeader(title = stringResource(R.string.mypage_my_comments_title), onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.items.isEmpty() -> EmptyStateMessage(stringResource(R.string.mypage_my_comments_empty))
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    uiState.items.forEach { comment ->
                        MyCommentCard(comment = comment, onGoToPostClick = { onGoToPostClick(comment.recruitId) })
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
private fun MyCommentCard(
    comment: MyComment,
    onGoToPostClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = comment.recruitTitle,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(
                label = comment.recruitStatus.label,
                containerColor = recruitStatusColor(comment.recruitStatus),
                contentColor = if (comment.recruitStatus == RecruitStatus.CLOSED) Color.Black else Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PickiiFieldBackground)
                    .padding(12.dp)
        ) {
            Text(text = comment.content, color = Color.Black, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.mypage_my_comments_label_date, comment.createdAt.toFullDisplayString()),
            color = PickiiTextGray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black)
                    .clickable(onClick = onGoToPostClick)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Text(
                text = stringResource(R.string.mypage_my_comments_button_go_to_post),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun recruitStatusColor(status: RecruitStatus): Color =
    when (status) {
        RecruitStatus.OPEN -> PickiiBlue
        RecruitStatus.CLOSED -> PickiiDisabledGray
        RecruitStatus.ADDITIONAL -> Color(0xFF66BB6A)
    }
