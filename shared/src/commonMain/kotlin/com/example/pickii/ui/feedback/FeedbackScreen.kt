package com.example.pickii.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.mypage_feedback_empty
import com.example.pickii.shared.generated.resources.mypage_feedback_title
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.EmptyStateMessage
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.PickiiTopBar
import com.example.pickii.ui.feedback.component.FeedbackTab
import com.example.pickii.ui.feedback.component.FeedbackTipCard
import com.example.pickii.ui.feedback.component.ReceivedFeedbackCard
import com.example.pickii.ui.feedback.component.WritableFeedbackCard
import com.example.pickii.ui.theme.PickiiPaletteBaseWhite
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FeedbackScreen(
    uiState: FeedbackUiState,
    onTabClick: (FeedbackTabType) -> Unit,
    onWriteFeedbackClick: (
        projectId: Long,
        memberId: Long
    ) -> Unit,
    onReceivedFeedbackClick: (projectId: Long) -> Unit,
    onNotificationClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCloseClick)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(PickiiPaletteBaseWhite)
    ) {
        FeedbackHeaderSection(
            selectedTab = uiState.selectedTab,
            onTabClick = onTabClick,
            onNotificationClick = onNotificationClick,
            onBackClick = onCloseClick
        )

        when {
            uiState.isLoading && uiState.projects.isEmpty() -> {
                LoadingIndicator(modifier = Modifier.weight(1f))
            }

            uiState.selectedTab == FeedbackTabType.RECEIVED && uiState.projects.isEmpty() -> {
                EmptyStateMessage(
                    message = stringResource(Res.string.mypage_feedback_empty),
                    modifier = Modifier.weight(1f)
                )
            }

            uiState.selectedTab == FeedbackTabType.WRITE -> {
                WritableFeedbackContent(
                    projects = uiState.projects,
                    onWriteFeedbackClick = onWriteFeedbackClick,
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                ReceivedFeedbackContent(
                    projects = uiState.projects,
                    onReceivedFeedbackClick = onReceivedFeedbackClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FeedbackHeaderSection(
    selectedTab: FeedbackTabType,
    onTabClick: (FeedbackTabType) -> Unit,
    onNotificationClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                )
    ) {
        PickiiTopBar(
            notificationCount = 0,
            onNotificationClick = onNotificationClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        BackHeader(
            title = stringResource(Res.string.mypage_feedback_title),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        FeedbackTab(
            selectedTab = selectedTab,
            onTabClick = onTabClick
        )
    }
}

@Composable
private fun WritableFeedbackContent(
    projects: List<FeedbackProjectUiModel>,
    onWriteFeedbackClick: (
        projectId: Long,
        memberId: Long
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 32.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FeedbackTipCard()
        }

        items(
            items = projects,
            key = { project -> project.id }
        ) { project ->
            WritableFeedbackCard(
                project = project,
                onWriteFeedbackClick = onWriteFeedbackClick
            )
        }
    }
}

@Composable
private fun ReceivedFeedbackContent(
    projects: List<FeedbackProjectUiModel>,
    onReceivedFeedbackClick: (projectId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 18.dp,
                bottom = 32.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = projects,
            key = { project -> project.id }
        ) { project ->
            ReceivedFeedbackCard(
                project = project,
                onClick = {
                    onReceivedFeedbackClick(project.id)
                }
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun FeedbackScreenPreview() {
    FeedbackScreen(
        uiState =
            FeedbackUiState(
                selectedTab = FeedbackTabType.WRITE,
                projects =
                    listOf(
                        FeedbackProjectUiModel(
                            id = 1L,
                            title = "서비스 기획 공모전",
                            startDate = "7월 4일",
                            endDate = "7월 6일",
                            remainingDays = 2,
                            completedCount = 1,
                            totalCount = 3,
                            canReadFeedback = true,
                            members =
                                listOf(
                                    FeedbackMemberUiModel(
                                        id = 1L,
                                        name = "김OO님",
                                        isCompleted = false
                                    ),
                                    FeedbackMemberUiModel(
                                        id = 2L,
                                        name = "이OO님",
                                        isCompleted = false
                                    ),
                                    FeedbackMemberUiModel(
                                        id = 3L,
                                        name = "박OO님",
                                        isCompleted = true
                                    )
                                )
                        )
                    )
            ),
        onTabClick = {},
        onWriteFeedbackClick = { _, _ -> },
        onReceivedFeedbackClick = {},
        onNotificationClick = {},
        onCloseClick = {}
    )
}
