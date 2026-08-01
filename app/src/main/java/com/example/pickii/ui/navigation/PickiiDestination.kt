package com.example.pickii.ui.navigation

/** 모집 글 id를 인자로 받는 경로들이 공통으로 사용하는 내비게이션 인자 이름. */
const val ARG_POST_ID = "postId"

/** 앱의 내비게이션 경로. */
sealed class PickiiDestination(
    val route: String
) {
    /** 스플래시 화면. */
    data object Splash : PickiiDestination("splash")

    /** 로그인 화면. */
    data object Login : PickiiDestination("login")

    /** 홈 화면. */
    data object Home : PickiiDestination("home")

    /** 공고 글 등록 화면. */
    data object RecruitCreate : PickiiDestination("recruit/create")

    /** 공고 상세 화면. */
    data class RecruitDetail(
        val postId: String
    ) : PickiiDestination("recruit/$postId/detail") {
        companion object {
            /** [androidx.navigation.compose.NavHost]에 등록할 경로 템플릿. */
            const val ROUTE = "recruit/{$ARG_POST_ID}/detail"
        }
    }

    /** 공고 지원 화면. */
    data class RecruitApply(
        val postId: String
    ) : PickiiDestination("recruit/$postId/apply") {
        companion object {
            /** [androidx.navigation.compose.NavHost]에 등록할 경로 템플릿. */
            const val ROUTE = "recruit/{$ARG_POST_ID}/apply"
        }
    }

    /** 공고 글 수정 화면. */
    data class RecruitEdit(
        val postId: String
    ) : PickiiDestination("recruit/$postId/edit") {
        companion object {
            /** [androidx.navigation.compose.NavHost]에 등록할 경로 템플릿. */
            const val ROUTE = "recruit/{$ARG_POST_ID}/edit"
        }
    }
}
