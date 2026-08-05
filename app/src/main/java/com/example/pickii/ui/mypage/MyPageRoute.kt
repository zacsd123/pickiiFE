package com.example.pickii.ui.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.pickii.ui.mypage.applications.ApplicationsScreen
import com.example.pickii.ui.mypage.feedback.FeedbackRoute
import com.example.pickii.ui.mypage.home.MyPageHomeScreen
import com.example.pickii.ui.mypage.mycomments.MyCommentsScreen
import com.example.pickii.ui.mypage.myrecruits.MyRecruitsScreen
import com.example.pickii.ui.mypage.profile.ProfileViewScreen
import com.example.pickii.ui.mypage.profile.edit.ProfileEditScreen
import com.example.pickii.ui.mypage.scraps.ScrapsScreen
import com.example.pickii.ui.mypage.settings.PasswordChangeScreen
import com.example.pickii.ui.mypage.settings.SettingsScreen
import com.example.pickii.ui.mypage.withdrawal.WithdrawalScreen

private enum class MyPageScreenType {
    HOME,
    PROFILE_VIEW,
    PROFILE_EDIT,
    APPLICATIONS,
    MY_RECRUITS,
    MY_COMMENTS,
    SCRAPS,
    FEEDBACK,
    SETTINGS,
    PASSWORD_CHANGE,
    WITHDRAWAL
}

/**
 * 마이페이지 내부 화면 전환을 관리한다(chat/calendar와 동일하게 자체 상태로 하위 화면을 전환한다).
 *
 * @param onTopLevelScreenChange 마이페이지 홈(최상위)인지 여부가 바뀔 때마다 호출된다. 하단 내비게이션 표시 여부에 쓰인다.
 * @param onCreateProfileClick 프로필이 없을 때 "프로필 만들기"에서 온보딩 화면으로 이동하는 콜백(앱 레벨 네비게이션)
 * @param onNavigateToApplicantList "작성 공고"의 지원자 조회 버튼에서 이동하는 콜백(앱 레벨 네비게이션)
 * @param onNavigateToRecruitEdit "작성 공고"의 공고 수정 버튼에서 이동하는 콜백(앱 레벨 네비게이션)
 * @param onNavigateToRecruitDetail "작성한 댓글"의 공고 글 바로가기에서 이동하는 콜백(앱 레벨 네비게이션)
 * @param onNavigateToChatRoom "지원 현황"의 채팅방 바로가기에서 이동하는 콜백(앱 레벨 네비게이션)
 * @param onLoggedOut 로그아웃/회원탈퇴 완료 후 로그인 화면으로 이동하는 콜백(앱 레벨 네비게이션)
 */
@Suppress("LongParameterList")
@Composable
fun MyPageRoute(
    onTopLevelScreenChange: (Boolean) -> Unit,
    onCreateProfileClick: () -> Unit,
    onNavigateToApplicantList: (postId: String) -> Unit,
    onNavigateToRecruitEdit: (postId: String) -> Unit,
    onNavigateToRecruitDetail: (postId: String) -> Unit,
    onNavigateToChatRoom: (roomId: String) -> Unit,
    onLoggedOut: () -> Unit,
    onNotificationClick: () -> Unit,
) {
    var currentScreen by rememberSaveable { mutableStateOf(MyPageScreenType.HOME.name) }

    LaunchedEffect(currentScreen) {
        onTopLevelScreenChange(currentScreen == MyPageScreenType.HOME.name)
    }

    when (MyPageScreenType.valueOf(currentScreen)) {
        MyPageScreenType.HOME -> {
            MyPageHomeScreen(
                onProfileClick = { currentScreen = MyPageScreenType.PROFILE_VIEW.name },
                onEditProfileClick = { currentScreen = MyPageScreenType.PROFILE_EDIT.name },
                onCreateProfileClick = onCreateProfileClick,
                onApplicationsClick = { currentScreen = MyPageScreenType.APPLICATIONS.name },
                onMyRecruitsClick = { currentScreen = MyPageScreenType.MY_RECRUITS.name },
                onScrapsClick = { currentScreen = MyPageScreenType.SCRAPS.name },
                onMyCommentsClick = { currentScreen = MyPageScreenType.MY_COMMENTS.name },
                onFeedbackClick = { currentScreen = MyPageScreenType.FEEDBACK.name },
                onSettingsClick = { currentScreen = MyPageScreenType.SETTINGS.name },
                onNotificationClick = onNotificationClick,
            )
        }

        MyPageScreenType.PROFILE_VIEW -> {
            ProfileViewScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onEditClick = { currentScreen = MyPageScreenType.PROFILE_EDIT.name }
            )
        }

        MyPageScreenType.PROFILE_EDIT -> {
            ProfileEditScreen(
                onBackClick = { currentScreen = MyPageScreenType.PROFILE_VIEW.name },
                onSaved = { currentScreen = MyPageScreenType.PROFILE_VIEW.name }
            )
        }

        MyPageScreenType.APPLICATIONS -> {
            ApplicationsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNavigateToChatRoom = onNavigateToChatRoom
            )
        }

        MyPageScreenType.MY_RECRUITS -> {
            MyRecruitsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNavigateToApplicantList = onNavigateToApplicantList,
                onNavigateToRecruitEdit = onNavigateToRecruitEdit
            )
        }

        MyPageScreenType.MY_COMMENTS -> {
            MyCommentsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNavigateToRecruitDetail = onNavigateToRecruitDetail
            )
        }

        MyPageScreenType.SCRAPS -> {
            ScrapsScreen(onBackClick = { currentScreen = MyPageScreenType.HOME.name })
        }

        MyPageScreenType.FEEDBACK -> {
            FeedbackRoute(onExit = { currentScreen = MyPageScreenType.HOME.name })
        }

        MyPageScreenType.SETTINGS -> {
            SettingsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNavigateToPasswordChange = { currentScreen = MyPageScreenType.PASSWORD_CHANGE.name },
                onNavigateToWithdrawal = { currentScreen = MyPageScreenType.WITHDRAWAL.name },
                onLoggedOut = onLoggedOut
            )
        }

        MyPageScreenType.PASSWORD_CHANGE -> {
            PasswordChangeScreen(
                onBackClick = { currentScreen = MyPageScreenType.SETTINGS.name },
                onSuccess = onLoggedOut
            )
        }

        MyPageScreenType.WITHDRAWAL -> {
            WithdrawalScreen(
                onBackClick = { currentScreen = MyPageScreenType.SETTINGS.name },
                onComplete = onLoggedOut
            )
        }
    }
}
