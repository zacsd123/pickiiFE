package com.example.pickii.domain.repository

import com.example.pickii.domain.model.SocialAccount

/** 계정 관리(비밀번호 변경, 회원 탈퇴, 소셜 계정 연동 조회/해제)를 담당한다. */
interface AccountRepository {
    /**
     * 로그인 상태에서 비밀번호를 변경한다. 가정 엔드포인트([com.example.pickii.data.remote.dto.ChangePasswordRequest] 참고) —
     * 백엔드 준비 전까지 실패할 수 있다.
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String
    ): Result<Unit>

    /** 회원 탈퇴(1-9). 계정/프로필 등은 삭제되지만 작성 글/댓글/채팅은 유지되고 작성자만 "알 수 없음"으로 바뀐다. */
    suspend fun withdraw(
        password: String,
        emailVerificationToken: String,
        dataDeletionAgreed: Boolean,
        rejoinPolicyAgreed: Boolean
    ): Result<Unit>

    /** 소셜 계정 연동 상태를 조회한다(1-13). */
    suspend fun getSocialAccounts(): Result<List<SocialAccount>>

    /** 소셜 계정 연동을 해제한다(1-13). */
    suspend fun unlinkSocialAccount(provider: String): Result<Unit>
}
