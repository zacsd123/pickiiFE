package com.example.pickii.ui.mypage.withdrawal

/** [WithdrawalScreen]에 표시되는 상태. */
data class WithdrawalUiState(
    val email: String = "",
    val isSendingEmailCode: Boolean = false,
    val isEmailCodeSent: Boolean = false,
    val emailCode: String = "",
    val isVerifyingEmailCode: Boolean = false,
    val isEmailVerified: Boolean = false,
    val emailVerificationToken: String? = null,
    val emailMessage: String? = null,
    val isEmailMessageError: Boolean = false,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isDataDeletionAgreed: Boolean = false,
    val isRejoinPolicyAgreed: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false
) {
    val isSubmitEnabled: Boolean
        get() =
            isEmailVerified &&
                password.isNotBlank() &&
                isDataDeletionAgreed &&
                isRejoinPolicyAgreed &&
                !isSubmitting
}
