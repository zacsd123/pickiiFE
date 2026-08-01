package com.example.pickii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pickii.ui.calendar.CalendarRoute
import com.example.pickii.ui.chat.ChatRoute
import com.example.pickii.ui.common.PickiiBottomNav
import com.example.pickii.ui.common.PickiiBottomNavTab
import com.example.pickii.ui.home.HomeScreen
import com.example.pickii.ui.login.LoginScreen
import com.example.pickii.ui.navigation.ARG_POST_ID
import com.example.pickii.ui.navigation.PickiiDestination
import com.example.pickii.ui.recruitapply.RecruitApplyScreen
import com.example.pickii.ui.recruitdetail.RecruitDetailScreen
import com.example.pickii.ui.recruitform.RecruitFormScreen
import com.example.pickii.ui.splash.SplashScreen
import com.example.pickii.ui.theme.PickiiTheme
import com.example.pickii.ui.theme.PickiiYellowLight
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /** 스플래시 화면을 먼저 띄우고, [PickiiNavHost]로 이후 화면 전환을 구성한다. */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PickiiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PickiiNavHost()
                }
            }
        }
    }
}

/** 앱의 전체 화면 전환을 담당하는 내비게이션 그래프. */
@Composable
private fun PickiiNavHost() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // 캘린더/채팅은 내부적으로 여러 하위 화면을 갖는데, 그중 최상위 화면(월간 캘린더/채팅 목록)일 때만 바텀 내비게이션을 보여준다.
    var isCalendarTopLevel by remember { mutableStateOf(true) }
    var isChatTopLevel by remember { mutableStateOf(true) }

    // 마이페이지는 아직 이동할 화면이 없어 선택 표시만 바꾼다. 실제 라우트가 바뀌면 이 표시는 초기화된다.
    var isMyPageSelected by remember { mutableStateOf(false) }
    LaunchedEffect(currentRoute) {
        isMyPageSelected = false
    }

    val currentTab =
        when (currentRoute) {
            PickiiDestination.Home.route -> PickiiBottomNavTab.HOME
            PickiiDestination.Calender.route -> PickiiBottomNavTab.CALENDAR
            PickiiDestination.Chat.route -> PickiiBottomNavTab.CHAT
            else -> null
        }

    val isBottomNavVisible =
        when (currentRoute) {
            PickiiDestination.Home.route -> true
            PickiiDestination.Calender.route -> isCalendarTopLevel
            PickiiDestination.Chat.route -> isChatTopLevel
            else -> false
        }

    Scaffold(
        containerColor = PickiiYellowLight,
        bottomBar = {
            if (isBottomNavVisible && currentTab != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PickiiBottomNav(
                        selectedTab = if (isMyPageSelected) PickiiBottomNavTab.MY_PAGE else currentTab,
                        onTabSelect = { tab ->
                            when (tab) {
                                PickiiBottomNavTab.HOME -> {
                                    isMyPageSelected = false
                                    if (currentRoute != PickiiDestination.Home.route) {
                                        navController.popBackStack(PickiiDestination.Home.route, inclusive = false)
                                    }
                                }
                                PickiiBottomNavTab.CALENDAR -> {
                                    isMyPageSelected = false
                                    if (currentRoute != PickiiDestination.Calender.route) {
                                        navController.navigateToTab(PickiiDestination.Calender.route)
                                    }
                                }
                                PickiiBottomNavTab.CHAT -> {
                                    isMyPageSelected = false
                                    if (currentRoute != PickiiDestination.Chat.route) {
                                        navController.navigateToTab(PickiiDestination.Chat.route)
                                    }
                                }
                                PickiiBottomNavTab.MY_PAGE -> {
                                    isMyPageSelected = true
                                }
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PickiiDestination.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(PickiiDestination.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(PickiiDestination.Login.route) {
                            popUpTo(PickiiDestination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(PickiiDestination.Login.route) {
                LoginScreen(
                    onLoginClick = { navController.navigateToHomeClearingBackStack() },
                    onGuestClick = { navController.navigateToHomeClearingBackStack() }
                )
            }

            composable(PickiiDestination.Home.route) {
                HomeScreen(
                    onRegisterPostClick = { navController.navigate(PickiiDestination.RecruitCreate.route) },
                    onPostDetailClick = { postId -> navController.navigate(PickiiDestination.RecruitDetail(postId).route) },
                    onPostApplyClick = { postId -> navController.navigate(PickiiDestination.RecruitApply(postId).route) }
                )
            }

            composable(
                route = PickiiDestination.RecruitDetail.ROUTE,
                arguments = listOf(navArgument(ARG_POST_ID) { type = NavType.StringType })
            ) {
                RecruitDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onApplyClick = { postId -> navController.navigate(PickiiDestination.RecruitApply(postId).route) },
                    onEditClick = { postId -> navController.navigate(PickiiDestination.RecruitEdit(postId).route) },
                    onDeletedNavigateHome = { navController.popBackStack(PickiiDestination.Home.route, inclusive = false) },
                    onNavigateToLogin = { navController.navigateToLoginClearingBackStack() }
                )
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

            composable(PickiiDestination.Chat.route) {
                ChatRoute(
                    onTopLevelScreenChange = { isChatTopLevel = it },
                )
            }

            composable(PickiiDestination.Calender.route) {
                CalendarRoute(
                    onScheduleClick = { },
                    onTopLevelScreenChange = { isCalendarTopLevel = it },
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

/** 홈/캘린더/채팅 탭 전환 시 백스택이 계속 쌓이지 않도록, 홈을 기준으로 상태를 저장/복원하며 이동한다. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        popUpTo(PickiiDestination.Home.route) { saveState = true }
        restoreState = true
    }
}
