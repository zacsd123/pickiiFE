package com.example.pickii.ui.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationRoute(
    onCloseClick: () -> Unit,
    onNotificationClick: (NotificationItemUiModel) -> Unit = {},
    viewModel: NotificationViewModel = koinViewModel()
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
