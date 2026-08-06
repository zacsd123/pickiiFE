package com.example.pickii.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.NotificationEntry
import com.example.pickii.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 알림 목록 조회(9-1), 읽음 처리(9-2, 9-3), 삭제(9-4)를 담당한다.
 */
@HiltViewModel
class NotificationViewModel
    @Inject
    constructor(
        private val repository: NotificationRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NotificationUiState())
        val uiState = _uiState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                repository
                    .getNotifications()
                    .onSuccess { notifications ->
                        _uiState.update {
                            it.copy(notifications = notifications.map { entry -> entry.toUiModel() })
                        }
                    }
            }
        }

        /**
         * 알림을 읽음 상태로 변경한다.
         */
        fun readNotification(notificationId: Long) {
            _uiState.update { currentState ->
                currentState.copy(
                    notifications =
                        currentState.notifications.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(isRead = true)
                            } else {
                                notification
                            }
                        }
                )
            }
            viewModelScope.launch { repository.readNotification(notificationId.toString()) }
        }

        /**
         * 모든 알림을 읽음 상태로 변경한다.
         */
        fun readAllNotifications() {
            _uiState.update { currentState ->
                currentState.copy(
                    notifications =
                        currentState.notifications.map { notification ->
                            notification.copy(isRead = true)
                        }
                )
            }
            viewModelScope.launch { repository.readAllNotifications() }
        }

        /**
         * 특정 알림을 삭제한다.
         */
        fun deleteNotification(notificationId: Long) {
            _uiState.update { currentState ->
                currentState.copy(
                    notifications =
                        currentState.notifications.filterNot { notification ->
                            notification.id == notificationId
                        }
                )
            }
            viewModelScope.launch { repository.deleteNotification(notificationId.toString()) }
        }

        private fun NotificationEntry.toUiModel(): NotificationItemUiModel =
            NotificationItemUiModel(
                id = id.toLong(),
                title = title,
                description = content,
                timeText = sentAt.toRelativeTimeText(),
                type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.OTHER),
                isRead = isRead,
                referenceType = referenceType,
                referenceId = referenceId
            )
    }

private fun LocalDateTime.toRelativeTimeText(): String {
    val minutes = Duration.between(this, LocalDateTime.now()).toMinutes()
    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        minutes < 60 * 24 -> "${minutes / 60}시간 전"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}일 전"
        else -> "%04d.%02d.%02d".format(year, monthValue, dayOfMonth)
    }
}
