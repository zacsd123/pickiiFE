package com.example.pickii.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.util.visiblePageNumbers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_PAGE_SIZE = 10

/** 서버에서 한 페이지를 읽어온 결과. [PagedListViewModel.fetchPage]가 반환한다. */
data class PagedFetchResult<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int
)

/**
 * 번호 페이지네이션 목록 화면(작성 공고/작성 댓글/스크랩/지원 현황 등)이 공통으로 쓰는
 * 로딩/페이지 이동/토스트 로직을 담은 베이스 클래스.
 *
 * 화면마다 다른 부분(실제 서버 호출, 공통 필드 외 상태)만 [fetchPage]와 [withPagedState]로 주입하면 된다.
 */
abstract class PagedListViewModel<T, S : PagedListUiState<T>>(
    initialState: S,
    private val loadFailureMessageRes: Int,
    private val pageSize: Int = DEFAULT_PAGE_SIZE
) : ViewModel() {
    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /** 서버 호출 한 번. `page`는 서버 규약대로 0-based로 전달된다. */
    protected abstract suspend fun fetchPage(
        page: Int,
        size: Int
    ): Result<PagedFetchResult<T>>

    /** 공통 페이지 필드만 바꾼 새 상태를 만든다. 구현은 보통 `copy(...)` 한 줄이면 된다. */
    protected abstract fun S.withPagedState(
        isLoading: Boolean = this.isLoading,
        items: List<T> = this.items,
        currentPage: Int = this.currentPage,
        totalPages: Int = this.totalPages,
        toastMessageRes: Int? = this.toastMessageRes
    ): S

    fun refresh() = load(_uiState.value.currentPage)

    protected fun load(page: Int) {
        viewModelScope.launch {
            _uiState.update { it.withPagedState(isLoading = true) }
            fetchPage(page - 1, pageSize)
                .onSuccess { result ->
                    _uiState.update {
                        it.withPagedState(
                            isLoading = false,
                            items = result.items,
                            currentPage = result.currentPage + 1,
                            totalPages = result.totalPages.coerceAtLeast(1)
                        )
                    }
                }.onFailure {
                    _uiState.update { it.withPagedState(isLoading = false, toastMessageRes = loadFailureMessageRes) }
                }
        }
    }

    val visiblePageNumbers: List<Int>
        get() = visiblePageNumbers(_uiState.value.currentPage, _uiState.value.totalPages)

    fun onPageClick(page: Int) = load(page)

    fun onPreviousPage() {
        if (_uiState.value.currentPage <= 1) return
        load(_uiState.value.currentPage - 1)
    }

    fun onNextPage() {
        if (_uiState.value.currentPage >= _uiState.value.totalPages) return
        load(_uiState.value.currentPage + 1)
    }

    fun onToastShown() {
        _uiState.update { it.withPagedState(toastMessageRes = null) }
    }
}
