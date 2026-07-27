package com.example.pickii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pickii.ui.home.HomeScreen
import com.example.pickii.ui.login.LoginScreen
import com.example.pickii.ui.navigation.ARG_POST_ID
import com.example.pickii.ui.navigation.PickiiDestination
import com.example.pickii.ui.recruitapply.RecruitApplyScreen
import com.example.pickii.ui.recruitdetail.RecruitDetailScreen
import com.example.pickii.ui.recruitform.RecruitFormScreen
import com.example.pickii.ui.splash.SplashScreen
import com.example.pickii.ui.theme.PickiiTheme
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

    NavHost(navController = navController, startDestination = PickiiDestination.Splash.route) {
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
