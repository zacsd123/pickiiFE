package com.example.pickii.ui.applicant

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pickii.R
import com.example.pickii.ui.common.LoadingIndicator

private val ApplicantBackgroundColor = Color(0xFFF9FCA8)
private val ApplicantCardColor = Color(0xFFFEFFF2)

private val PendingColor = Color(0xFFD6D7DD)
private val AcceptedColor = Color(0xFF82F363)
private val RejectedColor = Color(0xFFEE7045)

private val AcceptButtonColor = Color(0xFF627FF1)
private val RejectButtonColor = Color(0xFF171717)
private val DetailButtonColor = Color(0xFFF0F3A0)

private enum class ApplicantSortType(
    val displayName: String
) {
    OLDEST(
        displayName = "지원일자 오래된 순"
    ),
    LATEST(
        displayName = "지원일자 최신 순"
    )
}

@Composable
fun ApplicantListScreen(
    onBackClick: () -> Unit,
    onApplicantDetailClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApplicantListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var sortType by remember {
        mutableStateOf(ApplicantSortType.OLDEST)
    }

    val sortedApplicants =
        remember(
            uiState.applicants,
            sortType
        ) {
            when (sortType) {
                ApplicantSortType.OLDEST -> {
                    uiState.applicants.sortedBy { applicant ->
                        applicant.appliedDate
                    }
                }

                ApplicantSortType.LATEST -> {
                    uiState.applicants.sortedByDescending { applicant ->
                        applicant.appliedDate
                    }
                }
            }
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ApplicantBackgroundColor,
        bottomBar = {
            if (uiState.hasAcceptedApplicant) {
                GroupChatBottomBar(
                    onClick = viewModel::createGroupChat
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(ApplicantBackgroundColor)
                    .padding(innerPadding),
            contentPadding =
                PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 28.dp
                ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                ApplicantListHeader(
                    onBackClick = onBackClick
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                ApplicantFilterSection(
                    sortType = sortType,
                    onSortTypeChange = { selectedSortType ->
                        sortType = selectedSortType
                    },
                    pendingCount = uiState.pendingCount,
                    acceptedCount = uiState.acceptedCount,
                    rejectedCount = uiState.rejectedCount
                )
            }

            if (uiState.isLoading) {
                item {
                    LoadingIndicator(modifier = Modifier.fillMaxWidth().height(220.dp))
                }
            } else if (sortedApplicants.isEmpty()) {
                item {
                    EmptyApplicantCard()
                }
            } else {
                items(
                    items = sortedApplicants,
                    key = { applicant ->
                        applicant.id
                    }
                ) { applicant ->
                    ApplicantCard(
                        applicant = applicant,
                        onAcceptClick = {
                            viewModel.acceptApplicant(
                                applicantId = applicant.id
                            )
                        },
                        onRejectClick = {
                            viewModel.rejectApplicant(
                                applicantId = applicant.id
                            )
                        },
                        onDetailClick = {
                            onApplicantDetailClick(
                                applicant.id
                            )
                        },
                        onChatClick = {
                            viewModel.createDirectChat(applicant.memberId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicantListHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier =
                Modifier
                    .size(42.dp)
                    .clickable(
                        onClick = onBackClick
                    ),
            shape = CircleShape,
            color = Color(0xFFF9FCA8)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter =
                        painterResource(
                            id = R.drawable.ic_back
                        ),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = "지원자 조회",
            color = Color(0xFF171717),
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ApplicantFilterSection(
    sortType: ApplicantSortType,
    onSortTypeChange: (ApplicantSortType) -> Unit,
    pendingCount: Int,
    acceptedCount: Int,
    rejectedCount: Int
) {
    var isSortMenuExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                Row(
                    modifier =
                        Modifier
                            .clickable {
                                isSortMenuExpanded = true
                            }.padding(
                                vertical = 6.dp
                            ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sortType.displayName,
                        color = Color(0xFF444444),
                        fontSize = 14.sp
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Text(
                        text = "⌄",
                        color = Color(0xFF444444),
                        fontSize = 18.sp
                    )
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = {
                        isSortMenuExpanded = false
                    },
                    containerColor = ApplicantCardColor
                ) {
                    ApplicantSortType.entries.forEach { applicantSortType ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = applicantSortType.displayName
                                )
                            },
                            onClick = {
                                onSortTypeChange(applicantSortType)
                                isSortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            ApplicantStatusSummary(
                pendingCount = pendingCount,
                acceptedCount = acceptedCount,
                rejectedCount = rejectedCount
            )
        }
    }
}

@Composable
private fun ApplicantStatusSummary(
    pendingCount: Int,
    acceptedCount: Int,
    rejectedCount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApplicantStatusSummaryItem(
            color = PendingColor,
            title = "대기",
            count = pendingCount
        )

        ApplicantStatusSummaryItem(
            color = AcceptedColor,
            title = "수락",
            count = acceptedCount
        )

        ApplicantStatusSummaryItem(
            color = RejectedColor,
            title = "거절",
            count = rejectedCount
        )
    }
}

@Composable
private fun ApplicantStatusSummaryItem(
    color: Color,
    title: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(11.dp)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
        )

        Spacer(
            modifier = Modifier.width(4.dp)
        )

        Text(
            text = "$title $count",
            color = Color(0xFF666666),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ApplicantCard(
    applicant: ApplicantUiModel,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onDetailClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 9.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                ),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = ApplicantCardColor
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 18.dp,
                        end = 18.dp,
                        top = 18.dp,
                        bottom = 16.dp
                    )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                ApplicantProfilePlaceholder()

                Spacer(
                    modifier = Modifier.width(14.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = applicant.nickname,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF171717),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        ApplicantStatusBadge(
                            status = applicant.status
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    ApplicantInformationRow(
                        title = "지원 날짜",
                        value = applicant.appliedDate
                    )

                    if (applicant.applicationMessage.isNotBlank()) {
                        Spacer(
                            modifier = Modifier.height(7.dp)
                        )

                        ApplicantInformationRow(
                            title = "지원 메시지",
                            value = applicant.applicationMessage
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            ApplicantCardActions(
                status = applicant.status,
                onAcceptClick = onAcceptClick,
                onRejectClick = onRejectClick,
                onDetailClick = onDetailClick,
                onChatClick = onChatClick,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun ApplicantProfilePlaceholder() {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = Color(0xFFF3F3FA)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter =
                    painterResource(
                        id = R.drawable.ic_profile
                    ),
                contentDescription = "기본 프로필 이미지",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ApplicantInformationRow(
    title: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.width(74.dp),
            color = Color(0xFF9B9B9B),
            fontSize = 13.sp
        )

        Text(
            text = value,
            color = Color(0xFF282828),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ApplicantStatusBadge(
    status: ApplicantStatus,
    modifier: Modifier = Modifier
) {
    val containerColor =
        when (status) {
            ApplicantStatus.PENDING -> PendingColor
            ApplicantStatus.ACCEPTED -> AcceptedColor
            ApplicantStatus.REJECTED -> RejectedColor
        }

    val contentColor =
        when (status) {
            ApplicantStatus.PENDING -> Color(0xFF606060)
            ApplicantStatus.ACCEPTED -> Color(0xFF196D16)
            ApplicantStatus.REJECTED -> Color.White
        }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 3.dp
    ) {
        Text(
            text = status.displayName,
            modifier =
                Modifier.padding(
                    horizontal = 15.dp,
                    vertical = 9.dp
                ),
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ApplicantCardActions(
    status: ApplicantStatus,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onDetailClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (status) {
            ApplicantStatus.PENDING -> {
                ApplicantSmallButton(
                    text = "수락하기",
                    iconResId = R.drawable.ic_check,
                    containerColor = AcceptButtonColor,
                    contentColor = Color.White,
                    onClick = onAcceptClick
                )

                ApplicantSmallButton(
                    text = "거절하기",
                    iconResId = R.drawable.ic_close,
                    containerColor = RejectButtonColor,
                    contentColor = Color.White,
                    onClick = onRejectClick
                )
            }

            ApplicantStatus.ACCEPTED -> {
                ApplicantSmallButton(
                    text = "채팅방 개설",
                    iconResId = R.drawable.ic_speech_bubble,
                    containerColor = AcceptButtonColor,
                    contentColor = Color.White,
                    onClick = onChatClick
                )
            }

            ApplicantStatus.REJECTED -> Unit
        }

        ApplicantSmallButton(
            text = "자세히 보기",
            iconResId = R.drawable.ic_eye,
            containerColor = DetailButtonColor,
            contentColor = Color(0xFF73764A),
            onClick = onDetailClick
        )
    }
}

@Composable
private fun ApplicantSmallButton(
    text: String,
    iconResId: Int,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
        contentPadding =
            PaddingValues(
                horizontal = 12.dp,
                vertical = 0.dp
            ),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
    ) {
        Image(
            painter =
                painterResource(
                    id = iconResId
                ),
            contentDescription = null,
            modifier = Modifier.size(15.dp)
        )

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun GroupChatBottomBar(onClick: () -> Unit) {
    Surface(
        color = ApplicantBackgroundColor
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 14.dp
                    ),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(18.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF181818),
                        contentColor = Color.White
                    ),
                contentPadding =
                    PaddingValues(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    )
            ) {
                Image(
                    painter =
                        painterResource(
                            id = R.drawable.ic_group_chat
                        ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(
                    modifier = Modifier.width(7.dp)
                )

                Text(
                    text = "단체 채팅방 개설",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyApplicantCard() {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = ApplicantCardColor
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "아직 지원자가 없어요",
                    color = Color(0xFF202020),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "새로운 지원자가 생기면 이곳에 표시돼요.",
                    color = Color(0xFF8A8A8A),
                    fontSize = 13.sp
                )
            }
        }
    }
}
