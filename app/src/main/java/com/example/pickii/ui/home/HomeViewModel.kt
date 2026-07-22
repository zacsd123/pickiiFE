package com.example.pickii.ui.home

import androidx.lifecycle.ViewModel
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.ui.common.PickiiBottomNavTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** 교내/교외 모집 글 범위. */
enum class CampusScope(val label: String) {
    INTERNAL("교내"),
    EXTERNAL("교외")
}

/** 모집 글 카테고리(단일 선택). */
enum class RecruitCategory(val label: String) {
    COMPETITION("공모전 및 대회"),
    PROJECT("프로젝트"),
    STUDY("스터디")
}

/** 모집 글 주제(다중 선택). 일부 주제는 아직 지원하지 않아 [isEnabled]가 false다. */
enum class RecruitTopic(val label: String, val isEnabled: Boolean = true) {
    PLANNING("기획"),
    DESIGN("디자인"),
    RESEARCH("학술 연구"),
    IT("IT 개발"),
    VIDEO("영상 콘텐츠", isEnabled = false),
    ETC("기타", isEnabled = false)
}

/** 아직 서버 연동 전이라 사용하는 목업 모집 글 목록. */
private val MockRecruitPosts = List(30) { index ->
    RecruitPost(
        id = "post-$index",
        title = "서비스 기획 공모전",
        authorName = "김기획",
        date = "26.07.04",
        currentParticipants = 2,
        maxParticipants = 4
    )
}

/** 페이지네이션 한 페이지에 표시할 모집 글 개수. */
private const val PostsPerPage = 6

/** 페이지네이션에 한 번에 표시할 페이지 번호 개수. */
private const val PageWindowSize = 5

/** [HomeScreen]에 표시되는 상태. */
data class HomeUiState(
    val schoolName: String = "디지털서울문화예술대학교",
    val notificationCount: Int = 9,
    val searchQuery: String = "",
    val isFilterExpanded: Boolean = false,
    val campusScope: CampusScope = CampusScope.INTERNAL,
    val selectedCategory: RecruitCategory? = null,
    val selectedTopics: Set<RecruitTopic> = emptySet(),
    val posts: List<RecruitPost> = MockRecruitPosts,
    val currentPage: Int = 1,
    val selectedBottomNavTab: PickiiBottomNavTab = PickiiBottomNavTab.HOME
) {
    /** 전체 페이지 수. 모집 글이 없어도 최소 1페이지로 취급한다. */
    val totalPages: Int
        get() = if (posts.isEmpty()) 1 else (posts.size + PostsPerPage - 1) / PostsPerPage

    /** 현재 페이지에 해당하는 모집 글 목록. */
    val currentPagePosts: List<RecruitPost>
        get() = posts.drop((currentPage - 1) * PostsPerPage).take(PostsPerPage)

    /** 페이지네이션에 표시할 페이지 번호 목록. 전체 페이지가 적어도 그만큼만 보여준다. */
    val visiblePageNumbers: List<Int>
        get() {
            if (totalPages <= PageWindowSize) return (1..totalPages).toList()
            val start = (currentPage - PageWindowSize / 2).coerceIn(1, totalPages - PageWindowSize + 1)
            return (start until start + PageWindowSize).toList()
        }
}

/** 홈 화면의 검색/필터 상태와 모집 글 목록을 보관하고 갱신한다. */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun onToggleFilterPanel() {
        _uiState.update { it.copy(isFilterExpanded = !it.isFilterExpanded) }
    }

    fun onCampusScopeChange(scope: CampusScope) {
        _uiState.update { it.copy(campusScope = scope) }
    }

    /** 같은 카테고리를 다시 누르면 선택을 해제한다. */
    fun onCategorySelect(category: RecruitCategory) {
        _uiState.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
    }

    /** 비활성화된 주제는 무시하고, 활성화된 주제만 다중 선택 토글한다. */
    fun onTopicToggle(topic: RecruitTopic) {
        if (!topic.isEnabled) return
        _uiState.update {
            val selected = it.selectedTopics
            it.copy(
                selectedTopics = if (topic in selected) selected - topic else selected + topic
            )
        }
    }

    fun onResetFilters() {
        _uiState.update { it.copy(selectedCategory = null, selectedTopics = emptySet(), currentPage = 1) }
    }

    /** 필터 패널을 닫는다. 목업 데이터라 실제 필터링 결과는 백엔드 연동 후 반영한다. */
    fun onSearchClick() {
        _uiState.update { it.copy(isFilterExpanded = false, currentPage = 1) }
    }

    /** 이전 페이지로 이동한다. 첫 페이지에서는 아무 동작도 하지 않는다. */
    fun onPreviousPage() {
        _uiState.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(1)) }
    }

    /** 다음 페이지로 이동한다. 마지막 페이지에서는 아무 동작도 하지 않는다. */
    fun onNextPage() {
        _uiState.update { it.copy(currentPage = (it.currentPage + 1).coerceAtMost(it.totalPages)) }
    }

    /** 페이지 번호를 직접 클릭해 해당 페이지로 이동한다. */
    fun onPageClick(page: Int) {
        _uiState.update { it.copy(currentPage = page.coerceIn(1, it.totalPages)) }
    }

    /** 하단 내비게이션 탭의 선택 상태만 바꾼다. 대상 화면이 아직 없어 실제 이동은 하지 않는다. */
    fun onBottomNavTabSelect(tab: PickiiBottomNavTab) {
        _uiState.update { it.copy(selectedBottomNavTab = tab) }
    }
}
