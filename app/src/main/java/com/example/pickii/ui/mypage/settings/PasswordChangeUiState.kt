package com.example.pickii.ui.mypage.settings

/** 로그인 상태 비밀번호 변경 최소 글자 수. */
internal const val PASSWORD_CHANGE_MIN_LENGTH = 8

/** [PasswordChangeScreen]에 표시되는 상태. */
data class PasswordChangeUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val newPasswordConfirm: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val isMismatch: Boolean
        get() = newPasswordConfirm.isNotEmpty() && newPassword != newPasswordConfirm

    val isValid: Boolean
        get() =
            currentPassword.isNotBlank() &&
                newPassword.length >= PASSWORD_CHANGE_MIN_LENGTH &&
                newPassword == newPasswordConfirm
}
