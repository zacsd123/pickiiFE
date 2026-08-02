package com.example.pickii.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitPostSummary
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.RecruitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 서버 목록 조회 한 페이지 크기. */
private const val POSTS_PAGE_SIZE = 10

/** 페이지네이션에 한 번에 표시할 페이지 번호 개수. */
private const val PAGE_WINDOW_SIZE = 5

/** [HomeScreen]에 표시되는 상태. */
data class HomeUiState(
    val schoolName: String = "디지털서울문화예술대학교",
    val notificationCount: Int = 9,
    val searchQuery: String = "",
    val isFilterExpanded: Boolean = false,
    val campusScope: CampusScope = CampusScope.INTERNAL,
    val categories: List<RecruitCategory> = emptyList(),
    val topics: List<RecruitTopic> = emptyList(),
    val selectedCategory: RecruitCategory? = null,
    val selectedTopics: Set<RecruitTopic> = emptySet(),
    val posts: List<RecruitPostSummary> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalElements: Int = 0,
    val hasNext: Boolean = false,
    val isLoading: Boolean = false
) {
    /** 페이지네이션에 표시할 페이지 번호 목록. 전체 페이지가 적어도 그만큼만 보여준다. */
    val visiblePageNumbers: List<Int>
        get() {
            if (totalPages <= PAGE_WINDOW_SIZE) return (1..totalPages).toList()
            val start = (currentPage - PAGE_WINDOW_SIZE / 2).coerceIn(1, totalPages - PAGE_WINDOW_SIZE + 1)
            return (start until start + PAGE_WINDOW_SIZE).toList()
        }
}

/** 홈 화면의 검색/필터 상태를 보관하고, [RecruitRepository]/[MasterDataRepository]로 서버에서 목록을 조회한다. */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val recruitRepository: RecruitRepository,
        private val masterDataRepository: MasterDataRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            loadFilterOptions()
            refresh()
        }

        /** 필터 패널에 표시할 카테고리/주제 마스터 데이터를 불러온다. */
        private fun loadFilterOptions() {
            viewModelScope.launch {
                masterDataRepository.getCategories().onSuccess { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
            }
            viewModelScope.launch {
                masterDataRepository.getTopics().onSuccess { topics ->
                    _uiState.update { it.copy(topics = topics) }
                }
            }
        }

        /** 현재 검색어/필터/페이지 조건으로 서버에서 목록을 다시 조회한다. */
        fun refresh() {
            val state = _uiState.value
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                recruitRepository
                    .getPosts(
                        keyword = state.searchQuery.trim().ifBlank { null },
                        onCampus = state.campusScope == CampusScope.INTERNAL,
                        categoryIds = listOfNotNull(state.selectedCategory?.id),
                        topicIds = state.selectedTopics.map { it.id },
                        page = (state.currentPage - 1).coerceAtLeast(0),
                        size = POSTS_PAGE_SIZE
                    ).onSuccess { result ->
                        _uiState.update {
                            it.copy(
                                posts = result.posts,
                                totalPages = result.totalPages.coerceAtLeast(1),
                                totalElements = result.totalElements,
                                hasNext = result.hasNext,
                                isLoading = false
                            )
                        }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }

        fun onSearchQueryChange(value: String) {
            _uiState.update { it.copy(searchQuery = value) }
        }

        fun onToggleFilterPanel() {
            _uiState.update { it.copy(isFilterExpanded = !it.isFilterExpanded) }
        }

        /** 교내/교외 범위는 별도 확인 없이 바로 적용해 재조회한다. */
        fun onCampusScopeChange(scope: CampusScope) {
            _uiState.update { it.copy(campusScope = scope, currentPage = 1) }
            refresh()
        }

        /** 같은 카테고리를 다시 누르면 선택을 해제한다. 필터 패널의 "검색" 버튼을 눌러야 실제로 적용된다. */
        fun onCategorySelect(category: RecruitCategory) {
            _uiState.update {
                it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
            }
        }

        /** 다중 선택 토글. 필터 패널의 "검색" 버튼을 눌러야 실제로 적용된다. */
        fun onTopicToggle(topic: RecruitTopic) {
            _uiState.update {
                val selected = it.selectedTopics
                it.copy(selectedTopics = if (topic in selected) selected - topic else selected + topic)
            }
        }

        fun onResetFilters() {
            _uiState.update { it.copy(selectedCategory = null, selectedTopics = emptySet(), currentPage = 1) }
            refresh()
        }

        /** 검색어/필터 패널의 "검색"을 눌렀을 때, 지금까지 고른 조건으로 서버에 다시 조회한다. */
        fun onSearchClick() {
            _uiState.update { it.copy(isFilterExpanded = false, currentPage = 1) }
            refresh()
        }

        /** 이전 페이지로 이동한다. 첫 페이지에서는 아무 동작도 하지 않는다. */
        fun onPreviousPage() {
            if (_uiState.value.currentPage <= 1) return
            _uiState.update { it.copy(currentPage = it.currentPage - 1) }
            refresh()
        }

        /** 다음 페이지로 이동한다. 서버가 다음 페이지가 없다고 하면 아무 동작도 하지 않는다. */
        fun onNextPage() {
            if (!_uiState.value.hasNext) return
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            refresh()
        }

        /** 페이지 번호를 직접 클릭해 해당 페이지로 이동한다. */
        fun onPageClick(page: Int) {
            _uiState.update { it.copy(currentPage = page.coerceIn(1, it.totalPages)) }
            refresh()
        }
    }
