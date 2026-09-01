package com.example.pickii.ui.applicant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import com.example.pickii.ui.common.LocalSnackbarHostState
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ApplicantRoute(
    onBackClick: () -> Unit,
    onNavigateToChatRoom: (roomId: String) -> Unit,
    onNavigateToMemberProfile: (memberId: String) -> Unit,
    viewModel: ApplicantListViewModel = koinViewModel()
) {
    var currentScreen by remember {
        mutableStateOf<ApplicantScreen>(
            ApplicantScreen.List
        )
    }

    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.navigateToChatRoom.collect { roomId -> onNavigateToChatRoom(roomId) }
    }

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                snackbarHostState.showSnackbar(getString(event.messageRes))
        }
    }

    when (val screen = currentScreen) {
        ApplicantScreen.List -> {
            ApplicantListScreen(
                onBackClick = onBackClick,
                onApplicantDetailClick = { applicantId ->
                    currentScreen =
                        ApplicantScreen.Detail(
                            applicantId = applicantId
                        )
                },
                onProfileClick = { memberId -> onNavigateToMemberProfile(memberId.toString()) },
                viewModel = viewModel
            )
        }

        is ApplicantScreen.Detail -> {
            BackHandler {
                currentScreen = ApplicantScreen.List
            }

            ApplicantDetailScreen(
                applicant =
                    viewModel.getApplicant(
                        applicantId = screen.applicantId
                    ),
                onBackClick = {
                    currentScreen = ApplicantScreen.List
                },
                onAcceptClick = { applicantId ->
                    viewModel.acceptApplicant(
                        applicantId = applicantId
                    )
                },
                onRejectClick = { applicantId ->
                    viewModel.rejectApplicant(
                        applicantId = applicantId
                    )
                },
                onProfileClick = { memberId -> onNavigateToMemberProfile(memberId.toString()) }
            )
        }
    }

    BackHandler(
        enabled = currentScreen == ApplicantScreen.List
    ) {
        onBackClick()
    }
}
