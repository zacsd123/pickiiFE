package com.example.pickii.ui.navigation

/** 모집 글 id를 인자로 받는 경로들이 공통으로 사용하는 내비게이션 인자 이름. */
const val ARG_POST_ID = "postId"

/** 회원 id를 인자로 받는 경로들이 공통으로 사용하는 내비게이션 인자 이름. */
const val ARG_MEMBER_ID = "memberId"

/** 앱의 내비게이션 경로. */
sealed class PickiiDestination(
    val route: String
) {
    /** 스플래시 화면. */
    data object Splash : PickiiDestination("splash")

    /** 로그인 화면. */
    data object Login : PickiiDestination("login")

    /** 회원가입 화면(가입 폼 + 가입완료 화면을 한 화면에서 처리). */
    data object Signup : PickiiDestination("signup")

    /** 최초 로그인 시 이력서(프로필)를 입력받는 온보딩 화면. */
    data object Onboarding : PickiiDestination("onboarding")

    /** 비로그인 상태 비밀번호 재설정 화면. */
    data object PasswordReset : PickiiDestination("password-reset")

    /** 아이디 찾기 화면. */
    data object FindId : PickiiDestination("find-id")

    /** 홈 화면. */
    data object Home : PickiiDestination("home")

    /** 알림. */
    data object Notification : PickiiDestination("notification")

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

    /** 캘린더 화면 **/
    data object Calender : PickiiDestination("calender")

    /** 채팅 화면 **/
    data object Chat : PickiiDestination("chat")

    /** 마이페이지 화면(내부적으로 여러 하위 화면을 자체 상태로 전환한다). */
    data object MyPage : PickiiDestination("mypage")

    /** 마이페이지 "프로필을 만들어보세요"에서 진입하는 온보딩(이력서 생성) 화면. */
    data object OnboardingFromMyPage : PickiiDestination("mypage/onboarding")

    /** 공고 지원자 목록 화면(마이페이지의 "작성 공고"에서 진입). */
    data class ApplicantList(
        val postId: String
    ) : PickiiDestination("recruit/$postId/applicants") {
        companion object {
            /** [androidx.navigation.compose.NavHost]에 등록할 경로 템플릿. */
            const val ROUTE = "recruit/{$ARG_POST_ID}/applicants"
        }
    }

    /** 다른 회원의 프로필 조회 화면(10-1). 공고 작성자 닉네임 등에서 진입한다. */
    data class MemberProfile(
        val memberId: String
    ) : PickiiDestination("members/$memberId/profile") {
        companion object {
            /** [androidx.navigation.compose.NavHost]에 등록할 경로 템플릿. */
            const val ROUTE = "members/{$ARG_MEMBER_ID}/profile"
        }
    }
}
