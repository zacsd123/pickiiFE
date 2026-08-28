package com.example.pickii.ui.calendar.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * 일정 등록 화면과 ViewModel을 연결한다.
 */
@Composable
fun ScheduleEditorRoute(
    onBackClick: () -> Unit,
    onManageCategoryClick: () -> Unit,
    onScheduleSaved: () -> Unit,
    modifier: Modifier = Modifier,
    scheduleId: Long? = null,
    entryKey: Int = 0,
    viewModel: ScheduleEditorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(scheduleId, entryKey) {
        viewModel.initialize(scheduleId, entryKey)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onScheduleSaved()

            viewModel.onEvent(
                ScheduleEditorUiEvent.SavedStateConsumed
            )
        }
    }

    ScheduleEditorScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onManageCategoryClick = onManageCategoryClick,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
