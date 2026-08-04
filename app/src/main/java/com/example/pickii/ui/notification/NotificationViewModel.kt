package com.example.pickii.ui.notification

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * 알림 화면의 상태와 사용자 동작을 관리한다.
 */
@HiltViewModel
class NotificationViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        NotificationUiState(
            notifications = createMockNotifications(),
        ),
    )

    val uiState = _uiState.asStateFlow()

    /**
     * 알림을 읽음 상태로 변경한다.
     */
    fun readNotification(notificationId: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                notifications = currentState.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                },
            )
        }
    }

    /**
     * 모든 알림을 읽음 상태로 변경한다.
     */
    fun readAllNotifications() {
        _uiState.update { currentState ->
            currentState.copy(
                notifications = currentState.notifications.map { notification ->
                    notification.copy(isRead = true)
                },
            )
        }
    }

    /**
     * 특정 알림을 삭제한다.
     */
    fun deleteNotification(notificationId: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                notifications = currentState.notifications.filterNot { notification ->
                    notification.id == notificationId
                },
            )
        }
    }

    private fun createMockNotifications(): List<NotificationItemUiModel> {
        return listOf(
            NotificationItemUiModel(
                id = 1L,
                title = "민지님이 채팅방을 열었습니다.",
                description = "지금 바로 대화해보세요!",
                timeText = "방금 전",
                type = NotificationType.CHAT,
                isRead = false,
            ),
            NotificationItemUiModel(
                id = 2L,
                title = "지원이 수락됐어요!",
                description = "곧 채팅방이 열릴 거예요.",
                timeText = "10분 전",
                type = NotificationType.ACCEPT,
                isRead = false,
            ),
            NotificationItemUiModel(
                id = 3L,
                title = "수현님이 [제일기획 아이디어 공모전]에 지원했어요.",
                description = "프로필을 확인해보세요!",
                timeText = "1시간 전",
                type = NotificationType.APPLY,
                isRead = true,
            ),
            NotificationItemUiModel(
                id = 4L,
                title = "아쉽지만 [UX 공모전 팀원 모집] 공고가 마감됐어요.",
                description = "다른 공고도 살펴보세요!",
                timeText = "3일 전",
                type = NotificationType.CLOSED,
                isRead = true,
            ),
        )
    }
}
