package com.example.pickii.ui.mypage.scraps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.domain.model.MyScrap
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.ic_bookmark
import com.example.pickii.shared.generated.resources.mypage_scraps_empty
import com.example.pickii.shared.generated.resources.mypage_scraps_label_date
import com.example.pickii.shared.generated.resources.mypage_scraps_title
import com.example.pickii.ui.common.EmptyStateMessage
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.MyPageSectionHeader
import com.example.pickii.ui.common.PaginationRow
import com.example.pickii.ui.common.StatusBadge
import com.example.pickii.ui.common.recruitStatusColor
import com.example.pickii.ui.theme.PickiiPaletteBaseWhite
import com.example.pickii.ui.theme.PickiiPaletteBlue
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.util.toFullDisplayString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/** 스크랩한 공고 화면(17번). 목록에서 바로 스크랩 해제할 수 있다. */
@Composable
fun ScrapsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    viewModel: ScrapsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.toastMessageRes != null) {
        val messageRes = uiState.toastMessageRes
        LaunchedEffect(messageRes) {
            if (messageRes != null) Toast.makeText(context, getString(messageRes), Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    ScrapsScreenContent(
        uiState = uiState,
        visiblePageNumbers = viewModel.visiblePageNumbers,
        onBackClick = onBackClick,
        onNotificationClick = onNotificationClick,
        onUnscrapClick = viewModel::onUnscrapClick,
        onPageClick = viewModel::onPageClick,
        onPreviousPage = viewModel::onPreviousPage,
        onNextPage = viewModel::onNextPage
    )
}

@Suppress("LongParameterList")
@Composable
private fun ScrapsScreenContent(
    uiState: ScrapsUiState,
    visiblePageNumbers: List<Int>,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onUnscrapClick: (String) -> Unit,
    onPageClick: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(PickiiPaletteBaseWhite).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        MyPageSectionHeader(
            title = stringResource(Res.string.mypage_scraps_title),
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.items.isEmpty() -> EmptyStateMessage(stringResource(Res.string.mypage_scraps_empty))
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    uiState.items.forEach { scrap ->
                        ScrapCard(scrap = scrap, onUnscrapClick = { onUnscrapClick(scrap.recruitId) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PaginationRow(
                        currentPage = uiState.currentPage,
                        totalPages = uiState.totalPages,
                        visiblePageNumbers = visiblePageNumbers,
                        onPageClick = onPageClick,
                        onPreviousClick = onPreviousPage,
                        onNextClick = onNextPage
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrapCard(
    scrap: MyScrap,
    onUnscrapClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = scrap.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    label = scrap.status.label,
                    containerColor = recruitStatusColor(scrap.status),
                    contentColor = if (scrap.status == RecruitStatus.CLOSED) Color.Black else Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scrap.authorNickname, color = PickiiTextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.mypage_scraps_label_date, scrap.scrapedAt.toFullDisplayString()),
                color = PickiiTextGray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            painter = painterResource(Res.drawable.ic_bookmark),
            contentDescription = null,
            tint = PickiiPaletteBlue,
            modifier = Modifier.clickable(onClick = onUnscrapClick)
        )
    }
}
