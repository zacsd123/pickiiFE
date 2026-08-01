package com.example.pickii

import com.example.pickii.ui.calendar.monthly.MonthlyCalendarRoute
import com.example.pickii.ui.applicant.ApplicantRoute
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pickii.ui.chat.ChatListRoute
import com.example.pickii.ui.chat.ChatListViewModel
import com.example.pickii.ui.chat.ChatRoomPreviewUiModel
import com.example.pickii.ui.chat.ChatRoomRoute
import com.example.pickii.ui.chat.ChatRoomType
import com.example.pickii.ui.theme.PickiiTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pickii 애플리케이션의 시작 Activity다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

//        setContent {
//            PickiiTheme {
//                val chatListViewModel: ChatListViewModel = hiltViewModel()
//
//                var selectedChatRoom by remember {
//                    mutableStateOf<ChatRoomPreviewUiModel?>(null)
//                }
//
//                val currentChatRoom = selectedChatRoom
//
//                if (currentChatRoom == null) {
//                    ChatListRoute(
//                        viewModel = chatListViewModel,
//                        onChatRoomClick = { chatRoom ->
//                            selectedChatRoom = chatRoom
//                        },
//                    )
//                } else {
//                    ChatRoomRoute(
//                        roomId = currentChatRoom.id,
//                        roomTitle = if (
//                            currentChatRoom.type == ChatRoomType.GROUP
//                        ) {
//                            currentChatRoom.participantSummary.orEmpty()
//                        } else {
//                            currentChatRoom.senderName
//                        },
//                        roomType = currentChatRoom.type,
//                        onBackClick = {
//                            selectedChatRoom = null
//                        },
//                        onLeaveChatRoom = {
//                            chatListViewModel.removeChatRoom(
//                                roomId = currentChatRoom.id,
//                            )
//
//                            selectedChatRoom = null
//                        },
//                    )
//                }
//            }
//        }

//        setContent {
//            PickiiTheme {
//                ApplicantRoute(
//                    onBackClick = {
//                        finish()
//                    },
//                )
//            }
//        }

            setContent {
            PickiiTheme {

                MonthlyCalendarRoute(
                    onScheduleEditClick = {},
                    onAddScheduleClick = {},
                )

            }
        }
    }
}