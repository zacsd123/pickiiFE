package com.example.pickii.ui.applicant

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ApplicantRoute(
    onBackClick: () -> Unit,
    viewModel: ApplicantListViewModel = viewModel()
) {
    var currentScreen by remember {
        mutableStateOf<ApplicantScreen>(
            ApplicantScreen.List
        )
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
                }
            )
        }
    }

    BackHandler(
        enabled = currentScreen == ApplicantScreen.List
    ) {
        onBackClick()
    }
}
