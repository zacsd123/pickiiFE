package com.example.pickii.domain.model

/** 회원가입 약관 동의 상태(`POST /auth/signup`의 `terms`). */
data class SignupTerms(
    val ageAgreed: Boolean = false,
    val termsAgreed: Boolean = false,
    val privacyAgreed: Boolean = false,
    val profileShareAgreed: Boolean = false,
    val dataCollectionAgreed: Boolean = false,
    val pushNotiAgreed: Boolean = false
) {
    /** 필수 약관(전체 14세, 이용약관, 개인정보처리방침, 공고 지원 시 프로필 전달, 데이터 수집)을 모두 동의했는지 여부. */
    val isAllRequiredAgreed: Boolean
        get() = ageAgreed && termsAgreed && privacyAgreed && profileShareAgreed && dataCollectionAgreed

    /** 선택 약관까지 포함해 전부 동의했는지 여부("전체 선택" 체크박스 상태 계산용). */
    val isAllAgreed: Boolean
        get() = isAllRequiredAgreed && pushNotiAgreed
}
