package com.example.pickii.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.ui.common.PickiiBottomNav
import com.example.pickii.ui.common.PickiiBottomNavTab
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight

/** 필드/칩/버튼에 공통으로 사용하는 모서리 둥글기. */
private val ChipCornerRadius = 20.dp

/** 모집 글 카드 버튼의 높이. */
private val PostActionButtonHeight = 36.dp

/**
 * 앱의 메인 화면인 홈 화면.
 *
 * [HomeViewModel]에서 검색/필터 상태와 모집 글 목록을 받아와 [HomeScreenContent]에 전달한다.
 *
 * @param onRegisterPostClick 공고 등록 버튼 클릭 콜백
 * @param onPostDetailClick 모집 글의 상세보기 버튼 클릭 콜백
 * @param onPostApplyClick 모집 글의 지원하기 버튼 클릭 콜백
 * @param onNotificationClick 알림 아이콘 클릭 콜백
 * @param onCalendarClick 하단 캘린더 탭 클릭 콜백
 * @param onChatClick 하단 채팅 탭 클릭 콜백
 * @param onProfileClick 하단 프로필 탭 클릭 콜백
 */
@Composable
fun HomeScreen(
    onRegisterPostClick: () -> Unit = {},
    onPostDetailClick: (postId: String) -> Unit = {},
    onPostApplyClick: (postId: String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleFilterPanel = viewModel::onToggleFilterPanel,
        onCampusScopeChange = viewModel::onCampusScopeChange,
        onCategorySelect = viewModel::onCategorySelect,
        onTopicToggle = viewModel::onTopicToggle,
        onResetFilters = viewModel::onResetFilters,
        onSearchClick = viewModel::onSearchClick,
        onPreviousPageClick = viewModel::onPreviousPage,
        onNextPageClick = viewModel::onNextPage,
        onPageClick = viewModel::onPageClick,
        onBottomNavTabSelect = viewModel::onBottomNavTabSelect,
        onRegisterPostClick = onRegisterPostClick,
        onPostDetailClick = onPostDetailClick,
        onPostApplyClick = onPostApplyClick,
        onNotificationClick = onNotificationClick,
        onCalendarClick = onCalendarClick,
        onChatClick = onChatClick,
        onProfileClick = onProfileClick
    )
}

/** [HomeScreen]의 실제 UI. ViewModel 없이도 미리보기가 가능하도록 상태를 파라미터로 받는다. */
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onToggleFilterPanel: () -> Unit,
    onCampusScopeChange: (CampusScope) -> Unit,
    onCategorySelect: (RecruitCategory) -> Unit,
    onTopicToggle: (RecruitTopic) -> Unit,
    onResetFilters: () -> Unit,
    onSearchClick: () -> Unit,
    onPreviousPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    onPageClick: (Int) -> Unit,
    onBottomNavTabSelect: (PickiiBottomNavTab) -> Unit,
    onRegisterPostClick: () -> Unit,
    onPostDetailClick: (postId: String) -> Unit,
    onPostApplyClick: (postId: String) -> Unit,
    onNotificationClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        containerColor = PickiiYellowLight,
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PickiiBottomNav(
                    selectedTab = uiState.selectedBottomNavTab,
                    onTabSelect = { tab ->
                        onBottomNavTabSelect(tab)
                        when (tab) {
                            PickiiBottomNavTab.HOME -> Unit
                            PickiiBottomNavTab.CALENDAR -> onCalendarClick()
                            PickiiBottomNavTab.CHAT -> onChatClick()
                            PickiiBottomNavTab.MY_PAGE -> onProfileClick()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PickiiYellowLight)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HomeTopBar(
                schoolName = uiState.schoolName,
                notificationCount = uiState.notificationCount,
                onNotificationClick = onNotificationClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            SearchField(query = uiState.searchQuery, onQueryChange = onSearchQueryChange)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterToggleButton(onClick = onToggleFilterPanel)
                CampusScopeToggle(selected = uiState.campusScope, onSelect = onCampusScopeChange)
            }

            if (uiState.isFilterExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                FilterPanel(
                    selectedCategory = uiState.selectedCategory,
                    selectedTopics = uiState.selectedTopics,
                    onCategorySelect = onCategorySelect,
                    onTopicToggle = onTopicToggle,
                    onResetFilters = onResetFilters,
                    onSearchClick = onSearchClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_recruit_posts_title, uiState.posts.size),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                RegisterPostButton(onClick = onRegisterPostClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.currentPagePosts.chunked(2).forEach { rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowPosts.forEach { post ->
                        PostCard(
                            post = post,
                            onDetailClick = { onPostDetailClick(post.id) },
                            onApplyClick = { onPostApplyClick(post.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowPosts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            PaginationRow(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                visiblePageNumbers = uiState.visiblePageNumbers,
                onPageClick = onPageClick,
                onPreviousClick = onPreviousPageClick,
                onNextClick = onNextPageClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** 상단의 Pickii 로고, 학교명, 알림 아이콘. */
@Composable
private fun HomeTopBar(schoolName: String, notificationCount: Int, onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "P", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = "Pickii", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = schoolName,
            color = PickiiTextGray,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PickiiFieldBackground)
                .clickable(onClick = onNotificationClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )

            if (notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = notificationCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** 공고 제목/작성자를 검색하는 입력창. */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(text = stringResource(R.string.home_search_placeholder), color = PickiiTextGray, fontSize = 14.sp)
        },
        singleLine = true,
        shape = RoundedCornerShape(ChipCornerRadius),
        trailingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = PickiiTextGray) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

/** 필터 패널을 펼치고 접는 토글 버튼. */
@Composable
private fun FilterToggleButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .background(Color.Black)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Filled.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = stringResource(R.string.home_button_filter), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** 교내/교외 모집 글 범위를 고르는 세그먼트 토글. */
@Composable
private fun CampusScopeToggle(selected: CampusScope, onSelect: (CampusScope) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .background(PickiiFieldBackground)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CampusScope.entries.forEach { scope ->
            val isSelected = scope == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(ChipCornerRadius))
                    .background(if (isSelected) Color.Black else Color.Transparent)
                    .clickable { onSelect(scope) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = scope.label,
                    color = if (isSelected) Color.White else PickiiTextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** 카테고리/주제를 고르는 필터 패널. */
@Composable
private fun FilterPanel(
    selectedCategory: RecruitCategory?,
    selectedTopics: Set<RecruitTopic>,
    onCategorySelect: (RecruitCategory) -> Unit,
    onTopicToggle: (RecruitTopic) -> Unit,
    onResetFilters: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(text = stringResource(R.string.home_filter_category_title), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecruitCategory.entries.forEach { category ->
                FilterChip(
                    label = category.label,
                    selected = category == selectedCategory,
                    enabled = true,
                    onClick = { onCategorySelect(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = stringResource(R.string.home_filter_topic_title), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        RecruitTopic.entries.chunked(4).forEach { rowTopics ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTopics.forEach { topic ->
                    FilterChip(
                        label = topic.label,
                        selected = topic in selectedTopics,
                        enabled = topic.isEnabled,
                        onClick = { onTopicToggle(topic) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PickiiFieldBackground)
                    .clickable(onClick = onResetFilters)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.home_button_reset), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PickiiBlue)
                    .clickable(onClick = onSearchClick)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.home_button_search), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/** 카테고리/주제 필터에 사용되는 선택 가능한 칩. */
@Composable
private fun FilterChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .background(
                when {
                    !enabled -> PickiiDisabledGray
                    selected -> PickiiBlue
                    else -> Color.Black
                }
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** 새 모집 글을 등록하는 버튼. */
@Composable
private fun RegisterPostButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .background(Color.Black)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = stringResource(R.string.home_button_register_post), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** 모집 글 하나를 보여주는 카드. */
@Composable
private fun PostCard(post: RecruitPost, onDetailClick: () -> Unit, onApplyClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PickiiFieldBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = PickiiTextGray,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = post.title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "${post.authorName}, ${post.date}", color = PickiiTextGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${post.currentParticipants}/${post.maxParticipants}",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(PostActionButtonHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PickiiFieldBackground)
                    .clickable(onClick = onDetailClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.home_button_detail), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(PostActionButtonHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PickiiBlue)
                    .clickable(onClick = onApplyClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.home_button_apply), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/** 모집 글 목록의 페이지 번호. 현재 페이지는 검은 원으로 강조되고, 첫/마지막 페이지에서는 이전/다음 버튼이 비활성화된다. */
@Composable
private fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    visiblePageNumbers: List<Int>,
    onPageClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val hasPreviousPage = currentPage > 1
    val hasNextPage = currentPage < totalPages

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            color = if (hasPreviousPage) Color.Black else PickiiTextGray,
            fontSize = 14.sp,
            modifier = Modifier.clickable(enabled = hasPreviousPage, onClick = onPreviousClick)
        )

        Spacer(modifier = Modifier.width(10.dp))

        visiblePageNumbers.forEach { page ->
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.Black else Color.Transparent)
                    .clickable(enabled = !isSelected) { onPageClick(page) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.toString(),
                    color = if (isSelected) Color.White else PickiiTextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = ">",
            color = if (hasNextPage) Color.Black else PickiiTextGray,
            fontSize = 14.sp,
            modifier = Modifier.clickable(enabled = hasNextPage, onClick = onNextClick)
        )
    }
}

/** [HomeScreen]의 프리뷰 (필터 패널이 접힌 기본 상태). */
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            uiState = HomeUiState(),
            onSearchQueryChange = {},
            onToggleFilterPanel = {},
            onCampusScopeChange = {},
            onCategorySelect = {},
            onTopicToggle = {},
            onResetFilters = {},
            onSearchClick = {},
            onPreviousPageClick = {},
            onNextPageClick = {},
            onPageClick = {},
            onBottomNavTabSelect = {},
            onRegisterPostClick = {},
            onPostDetailClick = {},
            onPostApplyClick = {},
            onNotificationClick = {},
            onCalendarClick = {},
            onChatClick = {},
            onProfileClick = {}
        )
    }
}

/** [HomeScreen]의 프리뷰 (필터 패널이 펼쳐진 상태). */
@Preview(showBackground = true)
@Composable
private fun HomeScreenFilterExpandedPreview() {
    MaterialTheme {
        HomeScreenContent(
            uiState = HomeUiState(isFilterExpanded = true),
            onSearchQueryChange = {},
            onToggleFilterPanel = {},
            onCampusScopeChange = {},
            onCategorySelect = {},
            onTopicToggle = {},
            onResetFilters = {},
            onSearchClick = {},
            onPreviousPageClick = {},
            onNextPageClick = {},
            onPageClick = {},
            onBottomNavTabSelect = {},
            onRegisterPostClick = {},
            onPostDetailClick = {},
            onPostApplyClick = {},
            onNotificationClick = {},
            onCalendarClick = {},
            onChatClick = {},
            onProfileClick = {}
        )
    }
}
