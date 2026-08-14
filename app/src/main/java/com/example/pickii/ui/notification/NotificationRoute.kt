package com.example.pickii.ui.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NotificationRoute(
    onCloseClick: () -> Unit,
    onNotificationClick: (NotificationItemUiModel) -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationScreen(
        uiState = uiState,
        onCloseClick = onCloseClick,
        onNotificationClick = { notification ->
            viewModel.readNotification(notification.id)
            onNotificationClick(notification)
        }
    )
}
