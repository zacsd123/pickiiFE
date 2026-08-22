package com.example.pickii.ui.calendar.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * 태그 관리 화면과 ViewModel을 연결한다.
 */
@Composable
fun ScheduleCategoryRoute(
    onCloseClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleCategoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved()
            viewModel.consumeSavedState()
        }
    }

    ScheduleCategoryScreen(
        uiState = uiState,
        onCloseClick = onCloseClick,
        onCategoryEditClick = viewModel::startEditingCategory,
        onCategoryDeleteClick = viewModel::deleteCategory,
        onCategoryNameChange = viewModel::updateNewCategoryName,
        onColorSelect = viewModel::selectColor,
        onAddCategoryClick = viewModel::addOrUpdateCategory,
        onSaveClick = viewModel::saveCategories,
        modifier = modifier
    )
}
