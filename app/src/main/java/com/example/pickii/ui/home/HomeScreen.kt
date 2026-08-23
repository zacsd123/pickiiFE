package com.example.pickii.ui.home

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitPostSummary
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.ui.common.CampusScopeToggle
import com.example.pickii.ui.common.LevelAvatar
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.PaginationRow
import com.example.pickii.ui.common.PickiiBottomNavOverlaySpacing
import com.example.pickii.ui.common.PickiiTopBar
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.common.SelectableChip
import com.example.pickii.ui.common.StatusBadge
import com.example.pickii.ui.common.recruitStatusColor
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiPostCardBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import com.example.pickii.util.toCompactDisplayString
import org.koin.androidx.compose.koinViewModel

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
 */
@Composable
fun HomeScreen(
    onRegisterPostClick: () -> Unit = {},
    onPostDetailClick: (postId: String) -> Unit = {},
    onPostApplyClick: (postId: String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refresh()
        viewModel.loadNotificationCount()
    }

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                Toast
                    .makeText(
                        context,
                        event.messageRes,
                        Toast.LENGTH_SHORT
                    ).show()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchSubmit = viewModel::onSearchClick,
        onToggleFilterPanel = viewModel::onToggleFilterPanel,
        onCampusScopeChange = viewModel::onCampusScopeChange,
        onCategorySelect = viewModel::onCategorySelect,
        onTopicToggle = viewModel::onTopicToggle,
        onResetFilters = viewModel::onResetFilters,
        onSearchClick = viewModel::onSearchClick,
        onPreviousPageClick = viewModel::onPreviousPage,
        onNextPageClick = viewModel::onNextPage,
        onPageClick = viewModel::onPageClick,
        onRegisterPostClick = onRegisterPostClick,
        onPostDetailClick = onPostDetailClick,
        onPostApplyClick = onPostApplyClick,
        onNotificationClick = onNotificationClick
    )
}

/** [HomeScreen]의 실제 UI. ViewModel 없이도 미리보기가 가능하도록 상태를 파라미터로 받는다. */
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onToggleFilterPanel: () -> Unit,
    onCampusScopeChange: (CampusScope) -> Unit,
    onCategorySelect: (RecruitCategory) -> Unit,
    onTopicToggle: (RecruitTopic) -> Unit,
    onResetFilters: () -> Unit,
    onSearchClick: () -> Unit,
    onPreviousPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    onPageClick: (Int) -> Unit,
    onRegisterPostClick: () -> Unit,
    onPostDetailClick: (postId: String) -> Unit,
    onPostApplyClick: (postId: String) -> Unit,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PickiiYellowLight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = PickiiBottomNavOverlaySpacing)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PickiiTopBar(
            notificationCount = uiState.notificationCount,
            onNotificationClick = onNotificationClick,
            centerContent = {
                Text(
                    text = uiState.schoolName,
                    color = PickiiTextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SearchField(query = uiState.searchQuery, onQueryChange = onSearchQueryChange, onSubmit = onSearchSubmit)

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
                categories = uiState.categories,
                topics = uiState.topics,
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_recruit_posts_title, uiState.totalElements),
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            RegisterPostButton(onClick = onRegisterPostClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        uiState.posts.chunked(2).forEach { rowPosts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPosts.forEach { post ->
                    PostCard(
                        post = post,
                        isAuthor =
                            uiState.currentUserId != null &&
                                uiState.currentUserId == post.authorId,
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

/** 공고 제목/작성자를 검색하는 입력창. 검색 아이콘을 누르거나 키보드의 검색 버튼을 누르면 [onSubmit]이 호출된다. */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(text = stringResource(R.string.home_search_placeholder), color = PickiiTextGray, fontSize = 14.sp)
        },
        singleLine = true,
        shape = RoundedCornerShape(ChipCornerRadius),
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = PickiiTextGray,
                modifier = Modifier.clickable(onClick = onSubmit)
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        colors =
            OutlinedTextFieldDefaults.colors(
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
        modifier =
            Modifier
                .clip(RoundedCornerShape(ChipCornerRadius))
                .background(Color.Black)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.home_button_filter),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 카테고리/주제를 고르는 필터 패널. */
@Composable
private fun FilterPanel(
    categories: List<RecruitCategory>,
    topics: List<RecruitTopic>,
    selectedCategory: RecruitCategory?,
    selectedTopics: Set<RecruitTopic>,
    onCategorySelect: (RecruitCategory) -> Unit,
    onTopicToggle: (RecruitTopic) -> Unit,
    onResetFilters: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.home_filter_category_title),
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                SelectableChip(
                    label = category.label,
                    selected = category == selectedCategory,
                    enabled = true,
                    onClick = { onCategorySelect(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.home_filter_topic_title),
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topics.forEach { topic ->
                SelectableChip(
                    label = topic.label,
                    selected = topic in selectedTopics,
                    enabled = true,
                    onClick = { onTopicToggle(topic) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PickiiFieldBackground)
                        .clickable(onClick = onResetFilters)
                        .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_button_reset),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PickiiBlue)
                        .clickable(onClick = onSearchClick)
                        .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_button_search),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** 새 모집 글을 등록하는 버튼. */
@Composable
private fun RegisterPostButton(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(ChipCornerRadius))
                .background(Color.Black)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.home_button_register_post),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 모집 글 하나를 보여주는 카드. 카드 영역을 누르면 상세보기와 동일하게 상세 화면으로 이동한다. */
@Composable
private fun PostCard(
    post: RecruitPostSummary,
    isAuthor: Boolean,
    onDetailClick: () -> Unit,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClosed = post.status == RecruitStatus.CLOSED

    Column(
        modifier =
            modifier
                .alpha(if (isClosed) 0.75f else 1f)
                .clickable(onClick = onDetailClick)
                .clip(RoundedCornerShape(16.dp))
                .background(PickiiPostCardBackground)
                .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            LevelAvatar(exp = if (post.isAuthorUnknown) 0 else post.authorExperience, size = 36.dp)

            StatusBadge(
                label = post.status.label,
                containerColor = recruitStatusColor(post.status),
                contentColor =
                    if (post.status == RecruitStatus.CLOSED) {
                        Color.Black
                    } else {
                        Color.White
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = post.title,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text =
                "${
                    if (post.isAuthorUnknown) stringResource(R.string.unknown_author_nickname) else post.authorNickname
                }, ${post.createdAt.toCompactDisplayString()}",
            color = PickiiTextGray,
            fontSize = 12.sp
        )

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
                modifier =
                    Modifier
                        .weight(1f)
                        .height(PostActionButtonHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PickiiFieldBackground)
                        .clickable(onClick = onDetailClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_button_detail),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!isAuthor) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(PostActionButtonHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isClosed) PickiiFieldBackground else PickiiBlue
                            ).then(
                                if (!isClosed) {
                                    Modifier.clickable(onClick = onApplyClick)
                                } else {
                                    Modifier
                                }
                            ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.home_button_apply),
                        color = if (isClosed) PickiiTextGray else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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
            onSearchSubmit = {},
            onToggleFilterPanel = {},
            onCampusScopeChange = {},
            onCategorySelect = {},
            onTopicToggle = {},
            onResetFilters = {},
            onSearchClick = {},
            onPreviousPageClick = {},
            onNextPageClick = {},
            onPageClick = {},
            onRegisterPostClick = {},
            onPostDetailClick = {},
            onPostApplyClick = {},
            onNotificationClick = {}
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
            onSearchSubmit = {},
            onToggleFilterPanel = {},
            onCampusScopeChange = {},
            onCategorySelect = {},
            onTopicToggle = {},
            onResetFilters = {},
            onSearchClick = {},
            onPreviousPageClick = {},
            onNextPageClick = {},
            onPageClick = {},
            onRegisterPostClick = {},
            onPostDetailClick = {},
            onPostApplyClick = {},
            onNotificationClick = {}
        )
    }
}
