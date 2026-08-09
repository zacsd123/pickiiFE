package com.example.pickii.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.pickii.ui.theme.PickiiDivider
import com.example.pickii.ui.theme.PickiiGray400

private val MyMessageColor = Color(0xFF111111)
private val OtherMessageColor = Color(0xFFF9FCA8)

/**
 * 현재 사용자가 보낸 메시지를 표시한다. 시각/읽음 표시는 연속된 내 메시지 묶음의 마지막에만 보여준다.
 */
@Composable
internal fun MyChatMessage(
    message: ChatMessageUiModel,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isLastOfRun) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (message.isReadByCounterpart) "읽음" else "읽기 전",
                    color = Color(0xFFB4B868),
                    fontSize = 10.sp
                )

                Text(
                    text = message.createdAt.toChatRoomBubbleTimeText(),
                    color = PickiiGray400,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = message.content,
            modifier =
                Modifier
                    .fillMaxWidth(0.72f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    ).background(MyMessageColor)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 19.sp
        )
    }
}

/**
 * 상대방이 보낸 메시지를 표시한다. 시각은 연속된 상대 메시지 묶음의 마지막에만 보여준다.
 */
@Composable
internal fun OtherChatMessage(
    message: ChatMessageUiModel,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        ChatSenderAvatar(nickname = message.senderNickname)

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = message.content,
            modifier =
                Modifier
                    .fillMaxWidth(0.72f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 18.dp
                        )
                    ).background(OtherMessageColor)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),
            color = Color(0xFF374151),
            fontSize = 14.sp,
            lineHeight = 19.sp
        )

        if (isLastOfRun) {
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = message.createdAt.toChatRoomBubbleTimeText(),
                color = PickiiGray400,
                fontSize = 10.sp
            )
        }
    }
}

/** 상대방 메시지 왼쪽에 보여줄 발신자 아바타. 닉네임 첫 글자를 표시한다. */
@Composable
private fun ChatSenderAvatar(nickname: String) {
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PickiiDivider),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nickname.firstOrNull()?.toString().orEmpty(),
            color = PickiiGray400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private val ChatImageBubbleSize = 160.dp

/**
 * 현재 사용자가 보낸 사진 메시지를 표시한다.
 */
@Composable
internal fun MyImageMessage(
    message: ChatMessageUiModel,
    imageUri: String,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isLastOfRun) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (message.isReadByCounterpart) "읽음" else "읽기 전",
                    color = Color(0xFFB4B868),
                    fontSize = 10.sp
                )

                Text(
                    text = message.createdAt.toChatRoomBubbleTimeText(),
                    color = PickiiGray400,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        AsyncImage(
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(ChatImageBubbleSize)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
        )
    }
}

/**
 * 상대방이 보낸 사진 메시지를 표시한다.
 */
@Composable
internal fun OtherImageMessage(
    message: ChatMessageUiModel,
    imageUri: String,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        ChatSenderAvatar(nickname = message.senderNickname)

        Spacer(modifier = Modifier.width(8.dp))

        AsyncImage(
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(ChatImageBubbleSize)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 18.dp
                        )
                    )
        )

        if (isLastOfRun) {
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = message.createdAt.toChatRoomBubbleTimeText(),
                color = PickiiGray400,
                fontSize = 10.sp
            )
        }
    }
}
