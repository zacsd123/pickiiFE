package com.example.pickii

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pickii.ui.applicant.ApplicantRoute
import com.example.pickii.ui.calendar.CalendarRoute
import com.example.pickii.ui.chat.ChatRoute
import com.example.pickii.ui.common.LoginRequiredDialog
import com.example.pickii.ui.common.PickiiBottomNav
import com.example.pickii.ui.common.PickiiBottomNavTab
import com.example.pickii.ui.findid.FindIdScreen
import com.example.pickii.ui.home.HomeScreen
import com.example.pickii.ui.login.LoginScreen
import com.example.pickii.ui.memberprofile.MemberProfileScreen
import com.example.pickii.ui.mypage.MyPageRoute
import com.example.pickii.ui.mypage.MyPageScreenType
import com.example.pickii.ui.navigation.ARG_MEMBER_ID
import com.example.pickii.ui.navigation.ARG_POST_ID
import com.example.pickii.ui.navigation.MainNavigationViewModel
import com.example.pickii.ui.navigation.PickiiDestination
import com.example.pickii.ui.notification.NotificationRoute
import com.example.pickii.ui.notification.NotificationType
import com.example.pickii.ui.onboarding.OnboardingScreen
import com.example.pickii.ui.passwordreset.PasswordResetScreen
import com.example.pickii.ui.recruitapply.RecruitApplyScreen
import com.example.pickii.ui.recruitdetail.RecruitDetailScreen
import com.example.pickii.ui.recruitform.RecruitFormScreen
import com.example.pickii.ui.signup.SignupScreen
import com.example.pickii.ui.splash.SplashScreen
import com.example.pickii.ui.theme.PickiiTheme
import com.example.pickii.ui.theme.PickiiYellowLight
import dagger.hilt.android.AndroidEntryPoint

/** 화면 전환 페이드 애니메이션 길이(ms). 기본 전환 애니메이션이 무거운 화면(홈 등)과 겹치면 끊겨 보여서 가벼운 페이드로 대체한다. */
private const val NAV_TRANSITION_DURATION_MS = 200

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var notificationChatRoomId by mutableStateOf<Long?>(null)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    /** 스플래시 화면을 먼저 띄우고, [PickiiNavHost]로 이후 화면 전환을 구성한다. */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        hideSystemNavigationBar()
        requestNotificationPermissionIfNeeded()
        notificationChatRoomId = intent.chatRoomIdExtra()

        setContent {
            PickiiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PickiiNavHost(
                        notificationChatRoomId = notificationChatRoomId,
                        onNotificationChatRoomConsumed = { notificationChatRoomId = null }
                    )
                }
            }
        }
    }

    /** 채팅 알림을 탭했을 때(싱글톱이라 기존 인스턴스가 재사용됨) 새 방 id를 반영한다. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationChatRoomId = intent.chatRoomIdExtra()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted =
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /** 카메라 등 다른 앱을 다녀오면 시스템 내비게이션 바가 다시 나타나므로, 돌아올 때마다 다시 숨긴다. */
    override fun onResume() {
        super.onResume()
        hideSystemNavigationBar()
    }

    /**
     * 시스템 내비게이션 바(뒤로/홈/최근 버튼)를 숨겨서 화면 하단이 가려지지 않게 한다.
     * 화면 아래 끝에서 위로 스와이프하면 일시적으로 다시 나타난다(사라짐 방지 아님, 완전 차단 아님).
     */
    private fun hideSystemNavigationBar() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    companion object {
        /** 채팅 알림을 탭해 들어왔을 때, 바로 열어야 할 채팅방 id를 담는 인텐트 extra 키. */
        const val EXTRA_CHAT_ROOM_ID = "extra_chat_room_id"
    }
}

private fun Intent.chatRoomIdExtra(): Long? {
    val value = getLongExtra(MainActivity.EXTRA_CHAT_ROOM_ID, -1L)
    return value.takeIf { it != -1L }
}

/** 앱의 전체 화면 전환을 담당하는 내비게이션 그래프. */
@Composable
private fun PickiiNavHost(
    notificationChatRoomId: Long? = null,
    onNotificationChatRoomConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentRoute =
        navController
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

    // 캘린더/채팅/마이페이지는 내부적으로 여러 하위 화면을 갖는데, 그중 최상위 화면(월간 캘린더/채팅 목록/마이페이지 홈)일
    // 때만 바텀 내비게이션을 보여준다.
    var isCalendarTopLevel by remember { mutableStateOf(true) }
    var isChatTopLevel by remember { mutableStateOf(true) }
    var isMyPageTopLevel by remember { mutableStateOf(true) }

    val mainNavigationViewModel: MainNavigationViewModel = hiltViewModel()
    val isLoggedIn by mainNavigationViewModel.isLoggedIn.collectAsStateWithLifecycle()
    var showMyPageLoginPrompt by remember { mutableStateOf(false) }
    var showChatLoginPrompt by remember { mutableStateOf(false) }

    // "지원 현황"의 "채팅방 바로가기"에서 채팅 탭으로 이동할 때, 목록을 거치지 않고 바로 진입할 채팅방 id.
    var pendingChatRoomId by remember { mutableStateOf<Long?>(null) }

    // 알림 클릭으로 마이페이지 탭에 진입할 때, 홈을 거치지 않고 바로 보여줄 하위 화면(예: 지원 수락 알림 → 지원 현황).
    var pendingMyPageScreen by remember { mutableStateOf<MyPageScreenType?>(null) }

    // 채팅 알림을 탭해 들어온 경우. 스플래시를 지나 홈에 도달한 뒤에 채팅 탭으로 이동시킨다(스플래시 단계에서는
    // 아직 백스택에 홈이 없어 navigateToTab의 popUpTo가 아무 효과가 없다).
    LaunchedEffect(notificationChatRoomId, currentRoute) {
        if (notificationChatRoomId != null && currentRoute == PickiiDestination.Home.route) {
            pendingChatRoomId = notificationChatRoomId
            navController.navigateToTab(PickiiDestination.Chat.route)
            onNotificationChatRoomConsumed()
        }
    }

    val resolvedTab =
        when (currentRoute) {
            PickiiDestination.Home.route -> PickiiBottomNavTab.HOME
            PickiiDestination.Calender.route -> PickiiBottomNavTab.CALENDAR
            PickiiDestination.Chat.route -> PickiiBottomNavTab.CHAT
            PickiiDestination.MyPage.route -> PickiiBottomNavTab.MY_PAGE
            else -> null
        }

    // currentBackStackEntryAsState()가 탭 전환 도중 잠깐 다른 라우트를 거치면 currentTab이 null이 되는 순간이
    // 생기는데, 그때 PickiiBottomNav가 조합에서 완전히 빠졌다가 다시 들어가면서 인디케이터 애니메이션 상태가
    // 초기화돼 슬라이드 없이 바로 스냅해버린다. 마지막으로 확인된 탭을 계속 들고 있어서 이걸 막는다.
    var currentTab by remember { mutableStateOf<PickiiBottomNavTab?>(resolvedTab) }
    if (resolvedTab != null) {
        currentTab = resolvedTab
    }

    val isBottomNavVisible =
        when (currentRoute) {
            PickiiDestination.Home.route -> true
            PickiiDestination.Calender.route -> isCalendarTopLevel
            PickiiDestination.Chat.route -> isChatTopLevel
            PickiiDestination.MyPage.route -> isMyPageTopLevel
            else -> false
        }

    if (showMyPageLoginPrompt) {
        LoginRequiredDialog(
            onLoginClick = {
                showMyPageLoginPrompt = false
                navController.navigateToLoginClearingBackStack()
            },
            onDismiss = { showMyPageLoginPrompt = false }
        )
    }

    if (showChatLoginPrompt) {
        LoginRequiredDialog(
            onLoginClick = {
                showChatLoginPrompt = false
                navController.navigateToLoginClearingBackStack()
            },
            onDismiss = { showChatLoginPrompt = false }
        )
    }

    Scaffold(
        containerColor = PickiiYellowLight,
        bottomBar = {
            val tab = currentTab
            if (isBottomNavVisible && tab != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PickiiBottomNav(
                        selectedTab = tab,
                        onTabSelect = { tab ->
                            when (tab) {
                                PickiiBottomNavTab.HOME -> {
                                    if (currentRoute != PickiiDestination.Home.route) {
                                        navController.popBackStack(PickiiDestination.Home.route, inclusive = false)
                                    }
                                }
                                PickiiBottomNavTab.CALENDAR -> {
                                    if (currentRoute != PickiiDestination.Calender.route) {
                                        navController.navigateToTab(PickiiDestination.Calender.route)
                                    }
                                }
                                PickiiBottomNavTab.CHAT -> {
                                    if (!isLoggedIn) {
                                        showChatLoginPrompt = true
                                    } else if (currentRoute != PickiiDestination.Chat.route) {
                                        navController.navigateToTab(PickiiDestination.Chat.route)
                                    }
                                }
                                PickiiBottomNavTab.MY_PAGE -> {
                                    if (!isLoggedIn) {
                                        showMyPageLoginPrompt = true
                                    } else if (currentRoute != PickiiDestination.MyPage.route) {
                                        navController.navigateToTab(PickiiDestination.MyPage.route)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PickiiDestination.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION_MS)) },
            exitTransition = { fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION_MS)) },
            popEnterTransition = { fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION_MS)) },
            popExitTransition = { fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION_MS)) }
        ) {
            composable(PickiiDestination.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        // 자동 로그인: 저장된 Access Token이 아직 유효하면 로그인 화면을 건너뛴다.
                        val destination = if (isLoggedIn) PickiiDestination.Home.route else PickiiDestination.Login.route
                        navController.navigate(destination) {
                            popUpTo(PickiiDestination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(PickiiDestination.Login.route) {
                LoginScreen(
                    onNavigateHome = { navController.navigateToHomeClearingBackStack() },
                    onNavigateOnboarding = {
                        navController.navigateClearingBackStack(PickiiDestination.Onboarding.route)
                    },
                    onNavigateToPasswordReset = { navController.navigate(PickiiDestination.PasswordReset.route) },
                    onNavigateToFindId = { navController.navigate(PickiiDestination.FindId.route) },
                    onSignUpClick = { navController.navigate(PickiiDestination.Signup.route) },
                    onGuestClick = { navController.navigateToHomeClearingBackStack() }
                )
            }

            composable(PickiiDestination.PasswordReset.route) {
                PasswordResetScreen(
                    onBackClick = { navController.popBackStack() },
                    onComplete = { navController.popBackStack() }
                )
            }

            composable(PickiiDestination.FindId.route) {
                FindIdScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(PickiiDestination.Signup.route) {
                SignupScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateHome = { navController.navigateClearingBackStack(PickiiDestination.Home.route) },
                    onNavigateOnboarding = {
                        navController.navigateClearingBackStack(PickiiDestination.Onboarding.route)
                    }
                )
            }

            composable(PickiiDestination.Onboarding.route) {
                OnboardingScreen(
                    onFinished = { navController.navigateClearingBackStack(PickiiDestination.Home.route) }
                )
            }

            composable(
                route = PickiiDestination.Home.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                HomeScreen(
                    onRegisterPostClick = { navController.navigate(PickiiDestination.RecruitCreate.route) },
                    onPostDetailClick = { postId ->
                        navController.navigate(PickiiDestination.RecruitDetail(postId).route)
                    },
                    onPostApplyClick = { postId ->
                        navController.navigate(PickiiDestination.RecruitApply(postId).route)
                    },
                    onNotificationClick = {
                        navController.navigate(PickiiDestination.Notification.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(PickiiDestination.Notification.route) {
                NotificationRoute(
                    onCloseClick = { navController.popBackStack() },
                    onNotificationClick = { notification ->
                        val referenceId = notification.referenceId
                        when (notification.type) {
                            // 새 지원자 알림: 공고 상세가 아니라 그 공고의 지원자 확인 화면으로 바로 이동한다.
                            NotificationType.APPLY ->
                                referenceId?.let { postId ->
                                    navController.navigate(PickiiDestination.ApplicantList(postId).route)
                                }

                            // 지원 수락 알림: 마이페이지의 "지원 현황"으로 바로 이동한다.
                            NotificationType.ACCEPT -> {
                                pendingMyPageScreen = MyPageScreenType.APPLICATIONS
                                navController.navigateToTab(PickiiDestination.MyPage.route)
                            }

                            // 그 외(새 댓글 등)는 referenceType 기준으로 이동한다.
                            else ->
                                when (notification.referenceType) {
                                    "RECRUIT" ->
                                        referenceId?.let {
                                            navController.navigate(PickiiDestination.RecruitDetail(it).route)
                                        }

                                    "CHATROOM", "CHAT" ->
                                        referenceId?.toLongOrNull()?.let { roomId ->
                                            pendingChatRoomId = roomId
                                            navController.navigateToTab(PickiiDestination.Chat.route)
                                        }
                                    // PROJECT 등 그 외/미확인 referenceType은 ENUM.md 확인 전까지 이동하지 않는다.
                                }
                        }
                    }
                )
            }

            composable(
                route = PickiiDestination.RecruitDetail.ROUTE,
                arguments = listOf(navArgument(ARG_POST_ID) { type = NavType.StringType })
            ) {
                RecruitDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onAuthorProfileClick = { authorId ->
                        navController.navigate(PickiiDestination.MemberProfile(authorId).route)
                    },
                    onApplyClick = { postId -> navController.navigate(PickiiDestination.RecruitApply(postId).route) },
                    onEditClick = { postId -> navController.navigate(PickiiDestination.RecruitEdit(postId).route) },
                    onDeletedNavigateHome = {
                        navController.popBackStack(
                            PickiiDestination.Home.route,
                            inclusive = false
                        )
                    },
                    onNavigateToLogin = { navController.navigateToLoginClearingBackStack() }
                )
            }

            composable(
                route = PickiiDestination.MemberProfile.ROUTE,
                arguments = listOf(navArgument(ARG_MEMBER_ID) { type = NavType.StringType })
            ) {
                MemberProfileScreen(onBackClick = { navController.popBackStack() })
            }

            composable(
                route = PickiiDestination.RecruitApply.ROUTE,
                arguments = listOf(navArgument(ARG_POST_ID) { type = NavType.StringType })
            ) {
                RecruitApplyScreen(
                    onBackClick = { navController.popBackStack() },
                    onGoHomeClick = { navController.popBackStack(PickiiDestination.Home.route, inclusive = false) },
                    onNavigateToLogin = { navController.navigateToLoginClearingBackStack() }
                )
            }

            composable(PickiiDestination.RecruitCreate.route) {
                RecruitFormScreen(
                    onBackClick = { navController.popBackStack() },
                    onSubmitComplete = { navController.popBackStack(PickiiDestination.Home.route, inclusive = false) },
                    onNavigateToLogin = { navController.navigateToLoginClearingBackStack() }
                )
            }

            composable(
                route = PickiiDestination.RecruitEdit.ROUTE,
                arguments = listOf(navArgument(ARG_POST_ID) { type = NavType.StringType })
            ) {
                RecruitFormScreen(
                    onBackClick = { navController.popBackStack() },
                    onSubmitComplete = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigateToLoginClearingBackStack() }
                )
            }

            composable(
                route = PickiiDestination.Chat.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                ChatRoute(
                    onTopLevelScreenChange = { isChatTopLevel = it },
                    onNavigateToMemberProfile = { memberId ->
                        navController.navigate(PickiiDestination.MemberProfile(memberId.toString()).route)
                    },
                    initialRoomId = pendingChatRoomId
                )
            }

            composable(
                route = PickiiDestination.Calender.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                CalendarRoute(
                    onScheduleClick = { },
                    onTopLevelScreenChange = { isCalendarTopLevel = it }
                )
            }

            composable(
                route = PickiiDestination.MyPage.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                MyPageRoute(
                    onTopLevelScreenChange = { isMyPageTopLevel = it },
                    onCreateProfileClick = { navController.navigate(PickiiDestination.OnboardingFromMyPage.route) },
                    onNavigateToApplicantList = { postId ->
                        navController.navigate(PickiiDestination.ApplicantList(postId).route)
                    },
                    onNavigateToRecruitEdit = { postId ->
                        navController.navigate(PickiiDestination.RecruitEdit(postId).route)
                    },
                    onNavigateToRecruitDetail = { postId ->
                        navController.navigate(PickiiDestination.RecruitDetail(postId).route)
                    },
                    onNavigateToChatRoom = { roomId ->
                        pendingChatRoomId = roomId.toLongOrNull()
                        navController.navigateToTab(PickiiDestination.Chat.route)
                    },
                    onLoggedOut = { navController.navigateToLoginClearingBackStack() },
                    onNotificationClick = { navController.navigate(PickiiDestination.Notification.route) },
                    initialScreen = pendingMyPageScreen
                )
            }

            composable(PickiiDestination.OnboardingFromMyPage.route) {
                OnboardingScreen(onFinished = { navController.popBackStack() })
            }

            composable(
                route = PickiiDestination.ApplicantList.ROUTE,
                arguments = listOf(navArgument(ARG_POST_ID) { type = NavType.StringType })
            ) {
                ApplicantRoute(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToChatRoom = { roomId ->
                        pendingChatRoomId = roomId.toLongOrNull()
                        navController.navigateToTab(PickiiDestination.Chat.route)
                    }
                )
            }
        }
    }
}

/** 로그인/비회원 전환 후 홈으로 이동하며, 스플래시·로그인 화면은 백스택에서 제거한다. */
private fun NavHostController.navigateToHomeClearingBackStack() {
    navigate(PickiiDestination.Home.route) {
        popUpTo(PickiiDestination.Login.route) { inclusive = true }
    }
}

/** 로그인이 필요한 동작을 시도했을 때 백스택을 모두 비우고 로그인 화면으로 이동한다. */
private fun NavHostController.navigateToLoginClearingBackStack() {
    navigate(PickiiDestination.Login.route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
}

/**
 * [route]로 이동하며 백스택을 전부 비운다. 회원가입/온보딩처럼 그 전 화면이 백스택에서 이미 제거되었을 수도
 * 있는 지점에서 홈/온보딩으로 이동할 때 쓴다(대상이 백스택에 없어도 안전하게 동작한다).
 */
private fun NavHostController.navigateClearingBackStack(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
}

/** 홈/캘린더/채팅 탭 전환 시 백스택이 계속 쌓이지 않도록, 홈을 기준으로 상태를 저장/복원하며 이동한다. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        popUpTo(PickiiDestination.Home.route) { saveState = true }
        restoreState = true
    }
}

@Preview
@Composable
fun MainActivityPre() {
    PickiiTheme(
        darkTheme = false,
        dynamicColor = false,
        content = { }
    )
}
