package com.example.pickii.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NotificationBackgroundColor = Color(0xFFF8F8F5)
private val PrimaryTextColor = Color(0xFF20201F)
private val SecondaryTextColor = Color(0xFF8C8C87)
private val TimeTextColor = Color(0xFFB0B0AA)

private val UnreadCardBackgroundColor = Color(0xFFFFFEEE)
private val UnreadCardBorderColor = Color(0xFFE6E56E)
private val UnreadDotColor = Color(0xFFC6B91E)

private val ReadCardBackgroundColor = Color(0xFFFFFFFF)
private val ReadCardBorderColor = Color(0xFFE8E8E3)

private val ChatIconBackgroundColor = Color(0xFFDCE8FF)
private val ChatIconColor = Color(0xFF5B7FFF)

private val AcceptIconBackgroundColor = Color(0xFFDFF8E8)
private val AcceptIconColor = Color(0xFF55BF68)

private val ApplyIconBackgroundColor = Color(0xFFFBE7CF)
private val ApplyIconColor = Color(0xFFF07D26)

private val ClosedIconBackgroundColor = Color(0xFFFFF8BC)
private val ClosedIconColor = Color(0xFFC18F13)

private val BottomBarColor = Color(0xFF1C1C1B)
private val BottomSelectedColor = Color(0xFFF5FF9C)
private val BottomUnselectedColor = Color(0xFF858585)

@Composable
fun NotificationScreen(
    uiState: NotificationUiState,
    onCloseClick: () -> Unit,
    onNotificationClick: (NotificationItemUiModel) -> Unit,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onChatClick: () -> Unit,
    onMyPageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NotificationBackgroundColor)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            NotificationHeader(
                onCloseClick = onCloseClick,
            )

            Spacer(modifier = Modifier.height(25.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "알림",
                    color = PrimaryTextColor,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "최근 순",
                    color = TimeTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.notifications.isEmpty()) {
                EmptyNotificationContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 22.dp,
                        end = 22.dp,
                        top = 0.dp,
                        bottom = 130.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = uiState.notifications,
                        key = { notification -> notification.id },
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                onNotificationClick(notification)
                            },
                        )
                    }
                }
            }
        }

        PickiiBottomNavigation(
            selectedItem = BottomNavigationItem.HOME,
            onHomeClick = onHomeClick,
            onCalendarClick = onCalendarClick,
            onChatClick = onChatClick,
            onMyPageClick = onMyPageClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    start = 77.dp,
                    end = 77.dp,
                    bottom = 18.dp,
                ),
        )
    }
}

@Composable
private fun NotificationHeader(
    onCloseClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 28.dp,
                end = 28.dp,
                top = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1D1D1C)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "P",
                color = Color(0xFFF4FF91),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Spacer(modifier = Modifier.size(11.dp))

        Text(
            text = "Pickii",
            color = PrimaryTextColor,
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onCloseClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "알림 화면 닫기",
                tint = PrimaryTextColor,
                modifier = Modifier.size(29.dp),
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItemUiModel,
    onClick: () -> Unit,
) {
    val cardBackgroundColor =
        if (notification.isRead) ReadCardBackgroundColor else UnreadCardBackgroundColor

    val cardBorderColor =
        if (notification.isRead) ReadCardBorderColor else UnreadCardBorderColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(cardBackgroundColor)
            .border(
                width = 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .padding(
                start = 23.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 16.dp,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        NotificationTypeIcon(
            type = notification.type,
        )

        Spacer(modifier = Modifier.size(17.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Text(
                text = notification.title,
                color = PrimaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = notification.description,
                color = SecondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = notification.timeText,
                color = TimeTextColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
            )
        }

        if (!notification.isRead) {
            Spacer(modifier = Modifier.size(10.dp))

            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .border(
                        width = 2.dp,
                        color = UnreadDotColor,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun NotificationTypeIcon(
    type: NotificationType,
) {
    val iconData = when (type) {
        NotificationType.CHAT -> NotificationIconData(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            backgroundColor = ChatIconBackgroundColor,
            iconColor = ChatIconColor,
        )

        NotificationType.ACCEPT -> NotificationIconData(
            imageVector = Icons.Rounded.Check,
            backgroundColor = AcceptIconBackgroundColor,
            iconColor = AcceptIconColor,
        )

        NotificationType.APPLY -> NotificationIconData(
            imageVector = Icons.Rounded.Person,
            backgroundColor = ApplyIconBackgroundColor,
            iconColor = ApplyIconColor,
        )

        NotificationType.CLOSED -> NotificationIconData(
            imageVector = Icons.Outlined.Schedule,
            backgroundColor = ClosedIconBackgroundColor,
            iconColor = ClosedIconColor,
        )
    }

    Box(
        modifier = Modifier
            .size(53.dp)
            .clip(CircleShape)
            .background(iconData.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconData.imageVector,
            contentDescription = null,
            tint = iconData.iconColor,
            modifier = Modifier.size(25.dp),
        )
    }
}

@Composable
private fun EmptyNotificationContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "아직 받은 알림이 없습니다.",
            color = SecondaryTextColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PickiiBottomNavigation(
    selectedItem: BottomNavigationItem,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onChatClick: () -> Unit,
    onMyPageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(38.dp))
            .background(BottomBarColor)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BottomNavigationButton(
            item = BottomNavigationItem.HOME,
            isSelected = selectedItem == BottomNavigationItem.HOME,
            onClick = onHomeClick,
        )

        BottomNavigationButton(
            item = BottomNavigationItem.CALENDAR,
            isSelected = selectedItem == BottomNavigationItem.CALENDAR,
            onClick = onCalendarClick,
        )

        BottomNavigationButton(
            item = BottomNavigationItem.CHAT,
            isSelected = selectedItem == BottomNavigationItem.CHAT,
            onClick = onChatClick,
        )

        BottomNavigationButton(
            item = BottomNavigationItem.MY_PAGE,
            isSelected = selectedItem == BottomNavigationItem.MY_PAGE,
            onClick = onMyPageClick,
        )
    }
}

@Composable
private fun BottomNavigationButton(
    item: BottomNavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(52.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(
                if (isSelected) {
                    BottomSelectedColor
                } else {
                    Color.Transparent
                },
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (isSelected) 18.dp else 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = if (isSelected) PrimaryTextColor else BottomUnselectedColor,
            modifier = Modifier.size(25.dp),
        )

        if (isSelected) {
            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = item.label,
                color = PrimaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private data class NotificationIconData(
    val imageVector: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color,
)

private enum class BottomNavigationItem(
    val label: String,
    val contentDescription: String,
    val icon: ImageVector,
) {
    HOME(
        label = "홈",
        contentDescription = "홈",
        icon = Icons.Outlined.Home,
    ),
    CALENDAR(
        label = "캘린더",
        contentDescription = "캘린더",
        icon = Icons.Outlined.CalendarMonth,
    ),
    CHAT(
        label = "채팅",
        contentDescription = "채팅",
        icon = Icons.Outlined.ChatBubbleOutline,
    ),
    MY_PAGE(
        label = "마이페이지",
        contentDescription = "마이페이지",
        icon = Icons.Outlined.PersonOutline,
    ),
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun NotificationScreenPreview() {
    NotificationScreen(
        uiState = NotificationUiState(
            notifications = listOf(
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
            ),
        ),
        onCloseClick = {},
        onNotificationClick = {},
        onHomeClick = {},
        onCalendarClick = {},
        onChatClick = {},
        onMyPageClick = {},
    )
}
