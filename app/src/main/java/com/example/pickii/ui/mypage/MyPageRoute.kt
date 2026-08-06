package com.example.pickii.ui.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.pickii.ui.feedback.FeedbackRoute
import com.example.pickii.ui.mypage.applications.ApplicationsScreen
import com.example.pickii.ui.mypage.home.MyPageHomeScreen
import com.example.pickii.ui.mypage.mycomments.MyCommentsScreen
import com.example.pickii.ui.mypage.myrecruits.MyRecruitsScreen
import com.example.pickii.ui.mypage.profile.ProfileViewScreen
import com.example.pickii.ui.mypage.profile.edit.ProfileEditScreen
import com.example.pickii.ui.mypage.scraps.ScrapsScreen
import com.example.pickii.ui.mypage.settings.LogoutScreen
import com.example.pickii.ui.mypage.settings.PasswordChangeScreen
import com.example.pickii.ui.mypage.settings.SettingsScreen
import com.example.pickii.ui.mypage.withdrawal.WithdrawalScreen

/** 알림 클릭 등 앱 레벨에서 특정 하위 화면으로 바로 진입시킬 때 참조한다([MyPageRoute]의 [initialScreen]). */
enum class MyPageScreenType {
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
    WITHDRAWAL,
    LOGOUT
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
 * @param initialScreen 지정하면 마이페이지 홈을 거치지 않고 바로 해당 하위 화면으로 진입한다(예: 알림 클릭으로 "지원 현황" 진입).
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
    initialScreen: MyPageScreenType? = null
) {
    var currentScreen by rememberSaveable {
        mutableStateOf((initialScreen ?: MyPageScreenType.HOME).name)
    }

    // 채팅 탭과 동일하게, 마이페이지 탭이 하단 내비게이션에서 상태를 보존(restoreState)하기 때문에 이미 방문한
    // 적이 있으면 위 rememberSaveable 초기값은 무시된다. 매번 새로 지정된 값을 반영하려면 명시적으로 갱신한다.
    LaunchedEffect(initialScreen) {
        if (initialScreen != null) {
            currentScreen = initialScreen.name
        }
    }

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
                onNotificationClick = onNotificationClick,
                onNavigateToChatRoom = onNavigateToChatRoom
            )
        }

        MyPageScreenType.MY_RECRUITS -> {
            MyRecruitsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNotificationClick = onNotificationClick,
                onNavigateToApplicantList = onNavigateToApplicantList,
                onNavigateToRecruitEdit = onNavigateToRecruitEdit
            )
        }

        MyPageScreenType.MY_COMMENTS -> {
            MyCommentsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNotificationClick = onNotificationClick,
                onNavigateToRecruitDetail = onNavigateToRecruitDetail
            )
        }

        MyPageScreenType.SCRAPS -> {
            ScrapsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNotificationClick = onNotificationClick
            )
        }

        MyPageScreenType.FEEDBACK -> {
            FeedbackRoute(
                onExit = { currentScreen = MyPageScreenType.HOME.name },
                onNotificationClick = onNotificationClick
            )
        }

        MyPageScreenType.SETTINGS -> {
            SettingsScreen(
                onBackClick = { currentScreen = MyPageScreenType.HOME.name },
                onNavigateToPasswordChange = { currentScreen = MyPageScreenType.PASSWORD_CHANGE.name },
                onNavigateToWithdrawal = { currentScreen = MyPageScreenType.WITHDRAWAL.name },
                onNavigateToLogout = { currentScreen = MyPageScreenType.LOGOUT.name }
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

        MyPageScreenType.LOGOUT -> {
            LogoutScreen(
                onBackClick = { currentScreen = MyPageScreenType.SETTINGS.name },
                onLoggedOut = onLoggedOut
            )
        }
    }
}
