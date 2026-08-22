package com.example.pickii.ui.memberprofile

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.ui.mypage.profile.ProfileViewScreenContent
import org.koin.androidx.compose.koinViewModel

/** 다른 회원의 프로필 조회 화면(10-1). 공고 작성자 닉네임 등에서 진입한다. */
@Composable
fun MemberProfileScreen(
    onBackClick: () -> Unit,
    viewModel: MemberProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.toastMessageRes != null) {
        val messageRes = uiState.toastMessageRes
        LaunchedEffect(messageRes) {
            if (messageRes != null) Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    ProfileViewScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = null,
        title = stringResource(R.string.member_profile_title)
    )
}
