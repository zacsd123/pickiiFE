package com.example.pickii.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.repository.RecruitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 페이지네이션 한 페이지에 표시할 모집 글 개수. */
private const val POSTS_PER_PAGE = 6

/** 페이지네이션에 한 번에 표시할 페이지 번호 개수. */
private const val PAGE_WINDOW_SIZE = 5

/** [HomeScreen]에 표시되는 상태. */
data class HomeUiState(
    val schoolName: String = "디지털서울문화예술대학교",
    val notificationCount: Int = 9,
    val searchQuery: String = "",
    val isFilterExpanded: Boolean = false,
    val campusScope: CampusScope = CampusScope.INTERNAL,
    val selectedCategory: RecruitCategory? = null,
    val selectedTopics: Set<RecruitTopic> = emptySet(),
    val posts: List<RecruitPost> = emptyList(),
    val currentPage: Int = 1
) {
    /** 전체 페이지 수. 모집 글이 없어도 최소 1페이지로 취급한다. */
    val totalPages: Int
        get() = if (posts.isEmpty()) 1 else (posts.size + POSTS_PER_PAGE - 1) / POSTS_PER_PAGE

    /** 현재 페이지에 해당하는 모집 글 목록. */
    val currentPagePosts: List<RecruitPost>
        get() = posts.drop((currentPage - 1) * POSTS_PER_PAGE).take(POSTS_PER_PAGE)

    /** 페이지네이션에 표시할 페이지 번호 목록. 전체 페이지가 적어도 그만큼만 보여준다. */
    val visiblePageNumbers: List<Int>
        get() {
            if (totalPages <= PAGE_WINDOW_SIZE) return (1..totalPages).toList()
            val start = (currentPage - PAGE_WINDOW_SIZE / 2).coerceIn(1, totalPages - PAGE_WINDOW_SIZE + 1)
            return (start until start + PAGE_WINDOW_SIZE).toList()
        }
}

/** 홈 화면의 검색/필터 상태와 모집 글 목록을 보관하고 갱신한다. */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val recruitRepository: RecruitRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            observePosts()
        }

        /** [RecruitRepository]의 모집 글 목록을 구독해, 등록/수정/삭제 결과가 홈에 바로 반영되게 한다. */
        private fun observePosts() {
            viewModelScope.launch {
                recruitRepository.observePosts().collect { posts ->
                    _uiState.update { it.copy(posts = posts) }
                }
            }
        }

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
    }
