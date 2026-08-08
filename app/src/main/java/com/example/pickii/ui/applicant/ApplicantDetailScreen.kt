package com.example.pickii.ui.applicant

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R

private val ApplicantDetailBackgroundColor = Color(0xFFF9FCA8)
private val ApplicantDetailCardColor = Color(0xFFFEFFF2)

private val ApplicantDetailPendingColor = Color(0xFFD6D7DD)
private val ApplicantDetailAcceptedColor = Color(0xFF82F363)
private val ApplicantDetailRejectedColor = Color(0xFFEE7045)

@Composable
fun ApplicantDetailScreen(
    applicant: ApplicantUiModel?,
    onBackClick: () -> Unit,
    onAcceptClick: (Long) -> Unit,
    onRejectClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ApplicantDetailBackgroundColor
    ) { innerPadding ->
        if (applicant == null) {
            ApplicantNotFoundScreen(
                onBackClick = onBackClick,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            ApplicantDetailContent(
                applicant = applicant,
                onBackClick = onBackClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ApplicantDetailContent(
    applicant: ApplicantUiModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ApplicantDetailBackgroundColor)
                .verticalScroll(
                    state = rememberScrollState()
                ).padding(
                    start = 15.dp,
                    end = 15.dp,
                    top = 18.dp,
                    bottom = 30.dp
                ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ApplicantDetailHeader(
            onBackClick = onBackClick
        )

        ApplicantSummaryCard(
            applicant = applicant
        )

        ApplicantMessageCard(
            message = applicant.applicationMessage
        )

        ApplicantKeywordsCard(
            keywords = applicant.keywords
        )
    }
}

@Composable
private fun ApplicantDetailHeader(onBackClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(30.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Color.Black,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable(
                        onClick = onBackClick
                    ).padding(4.dp)
        )

        Text(
            text = "지원서 상세",
            color = Color(0xFF171717),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ApplicantSummaryCard(applicant: ApplicantUiModel) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                ),
        shape = RoundedCornerShape(15.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = ApplicantDetailCardColor
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 15.dp,
                        end = 15.dp,
                        top = 15.dp,
                        bottom = 15.dp
                    ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ApplicantDetailProfileImage()

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = applicant.nickname,
                    color = Color(0xFF171717),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = applicant.appliedDate,
                    color = Color(0xFF9A9A9A),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            ApplicantDetailStatusBadge(
                status = applicant.status
            )
        }
    }
}

@Composable
private fun ApplicantDetailProfileImage() {
    Surface(
        modifier = Modifier.size(52.dp),
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
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ApplicantDetailStatusBadge(status: ApplicantStatus) {
    val containerColor =
        when (status) {
            ApplicantStatus.PENDING -> ApplicantDetailPendingColor
            ApplicantStatus.ACCEPTED -> ApplicantDetailAcceptedColor
            ApplicantStatus.REJECTED -> ApplicantDetailRejectedColor
        }

    val contentColor =
        when (status) {
            ApplicantStatus.PENDING -> Color(0xFF5F5F5F)
            ApplicantStatus.ACCEPTED -> Color(0xFF196D16)
            ApplicantStatus.REJECTED -> Color.White
        }

    Surface(
        shape = CircleShape,
        color = containerColor
    ) {
        Text(
            text = status.displayName,
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ApplicantMessageCard(message: String) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color.Black.copy(alpha = 0.07f),
                    spotColor = Color.Black.copy(alpha = 0.07f)
                ),
        shape = RoundedCornerShape(15.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = ApplicantDetailCardColor
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
                        horizontal = 15.dp,
                        vertical = 15.dp
                    )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter =
                        painterResource(
                            id = R.drawable.ic_document
                        ),
                    contentDescription = "지원 메시지",
                    modifier = Modifier.size(16.dp)
                )

                Spacer(
                    modifier = Modifier.width(7.dp)
                )

                Text(
                    text = "지원 메시지",
                    color = Color(0xFF262626),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text =
                    message.ifBlank {
                        "작성된 지원 메시지가 없습니다."
                    },
                color = Color(0xFF444444),
                fontSize = 12.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun ApplicantKeywordsCard(keywords: List<String>) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color.Black.copy(alpha = 0.07f),
                    spotColor = Color.Black.copy(alpha = 0.07f)
                ),
        shape = RoundedCornerShape(15.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = ApplicantDetailCardColor
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
                        horizontal = 15.dp,
                        vertical = 15.dp
                    )
        ) {
            Text(
                text = "선택한 지원 키워드",
                color = Color(0xFF262626),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (keywords.isEmpty()) {
                Text(
                    text = "선택한 지원 키워드가 없습니다.",
                    color = Color(0xFF9A9A9A),
                    fontSize = 12.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    keywords.forEach { keyword ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFF0F3A0)
                        ) {
                            Text(
                                text = keyword,
                                modifier =
                                    Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                color = Color(0xFF73764A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicantNotFoundScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ApplicantDetailBackgroundColor)
                .padding(20.dp)
    ) {
        ApplicantDetailHeader(
            onBackClick = onBackClick
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "지원자 정보를 찾을 수 없어요.",
                color = Color(0xFF222222),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
