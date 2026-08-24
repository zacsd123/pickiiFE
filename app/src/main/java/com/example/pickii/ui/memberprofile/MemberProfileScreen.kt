package com.example.pickii.ui.memberprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.member_profile_title
import com.example.pickii.ui.common.LocalSnackbarHostState
import com.example.pickii.ui.mypage.profile.ProfileViewScreenContent
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/** 다른 회원의 프로필 조회 화면(10-1). 공고 작성자 닉네임 등에서 진입한다. */
@Composable
fun MemberProfileScreen(
    onBackClick: () -> Unit,
    viewModel: MemberProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    if (uiState.toastMessageRes != null) {
        val messageRes = uiState.toastMessageRes
        LaunchedEffect(messageRes) {
            if (messageRes != null) snackbarHostState.showSnackbar(getString(messageRes))
            viewModel.onToastShown()
        }
    }

    ProfileViewScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = null,
        title = stringResource(Res.string.member_profile_title)
    )
}
