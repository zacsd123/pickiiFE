package com.example.pickii.ui.mypage.mycomments

import com.example.pickii.domain.model.MyComment
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.mycomments_toast_load_failed
import com.example.pickii.ui.common.PagedFetchResult
import com.example.pickii.ui.common.PagedListViewModel
import org.jetbrains.compose.resources.StringResource

/** 작성한 댓글 목록(4-6)을 조회한다. */
class MyCommentsViewModel
    constructor(
        private val repository: MyPageActivityRepository
    ) : PagedListViewModel<MyComment, MyCommentsUiState>(
            initialState = MyCommentsUiState(),
            loadFailureMessageRes = Res.string.mycomments_toast_load_failed
        ) {
        init {
            refresh()
        }

        override suspend fun fetchPage(
            page: Int,
            size: Int
        ): Result<PagedFetchResult<MyComment>> =
            repository
                .getMyComments(
                    page = page,
                    size = size
                ).map { PagedFetchResult(it.items, it.currentPage, it.totalPages) }

        override fun MyCommentsUiState.withPagedState(
            isLoading: Boolean,
            items: List<MyComment>,
            currentPage: Int,
            totalPages: Int,
            toastMessageRes: StringResource?
        ): MyCommentsUiState =
            copy(
                isLoading = isLoading,
                items = items,
                currentPage = currentPage,
                totalPages = totalPages,
                toastMessageRes = toastMessageRes
            )
    }
