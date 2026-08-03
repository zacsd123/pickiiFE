package com.example.pickii.ui.mypage.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.pickii.ui.mypage.feedback.memberlist.FeedbackMemberListScreen
import com.example.pickii.ui.mypage.feedback.projectlist.FeedbackProjectListScreen

private enum class FeedbackScreenType {
    PROJECT_LIST,
    MEMBER_LIST
}

/** 상호평가/피드백 내부 화면 전환(프로젝트 목록 → 팀원별 평가 여부)을 관리한다. */
@Composable
fun FeedbackRoute(onExit: () -> Unit) {
    var currentScreen by rememberSaveable { mutableStateOf(FeedbackScreenType.PROJECT_LIST.name) }
    var selectedProjectId by rememberSaveable { mutableStateOf("") }

    when (FeedbackScreenType.valueOf(currentScreen)) {
        FeedbackScreenType.PROJECT_LIST -> {
            FeedbackProjectListScreen(
                onBackClick = onExit,
                onProjectClick = { projectId ->
                    selectedProjectId = projectId
                    currentScreen = FeedbackScreenType.MEMBER_LIST.name
                }
            )
        }

        FeedbackScreenType.MEMBER_LIST -> {
            FeedbackMemberListScreen(
                projectId = selectedProjectId,
                onBackClick = { currentScreen = FeedbackScreenType.PROJECT_LIST.name }
            )
        }
    }
}
