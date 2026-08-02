package com.example.pickii.ui.calendar.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.calendar.editor.component.ScheduleCategorySection
import com.example.pickii.ui.calendar.editor.component.ScheduleDateTimeSection
import com.example.pickii.ui.calendar.editor.component.ScheduleLocationField
import com.example.pickii.ui.calendar.editor.component.ScheduleMemoField
import com.example.pickii.ui.calendar.editor.component.ScheduleRepeatSection
import com.example.pickii.ui.calendar.editor.component.ScheduleTitleField

private val EditorBackgroundColor = Color(0xFFF7F7F2)
private val EditorHeaderBackgroundColor = Color(0xFFFFFFFF)
private val EditorHeaderTitleColor = Color(0xFF1B1B1B)
private val EditorBackButtonColor = Color(0xFF1B1B1B)

private val SaveButtonEnabledColor = Color(0xFF1B1B1B)
private val SaveButtonDisabledColor = Color(0xFFD6D6D0)
private val SaveButtonTextColor = Color(0xFFFFFFFF)

/**
 * 일정 등록 화면이다.
 */
@Composable
fun ScheduleEditorScreen(
    uiState: ScheduleEditorUiState,
    onBackClick: () -> Unit,
    onManageCategoryClick: () -> Unit,
    onEvent: (ScheduleEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(EditorBackgroundColor)
    ) {
        ScheduleEditorHeader(
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 40.dp
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ScheduleTitleField(
                    title = uiState.title,
                    onTitleChange = { title ->
                        onEvent(
                            ScheduleEditorUiEvent.TitleChanged(title)
                        )
                    }
                )
            }

            item {
                ScheduleCategorySection(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategoryClick = { categoryId ->
                        onEvent(
                            ScheduleEditorUiEvent.CategorySelected(
                                categoryId
                            )
                        )
                    },
                    onManageClick = onManageCategoryClick
                )
            }

            item {
                ScheduleDateTimeSection(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    isAllDay = uiState.isAllDay,
                    onStartDateChange = { date ->
                        onEvent(
                            ScheduleEditorUiEvent.StartDateChanged(date)
                        )
                    },
                    onEndDateChange = { date ->
                        onEvent(
                            ScheduleEditorUiEvent.EndDateChanged(date)
                        )
                    },
                    onStartTimeChange = { time ->
                        onEvent(
                            ScheduleEditorUiEvent.StartTimeChanged(time)
                        )
                    },
                    onEndTimeChange = { time ->
                        onEvent(
                            ScheduleEditorUiEvent.EndTimeChanged(time)
                        )
                    },
                    onAllDayChange = { isAllDay ->
                        onEvent(
                            ScheduleEditorUiEvent.AllDayChanged(isAllDay)
                        )
                    }
                )
            }

            item {
                ScheduleLocationField(
                    location = uiState.location,
                    onLocationChange = { location ->
                        onEvent(
                            ScheduleEditorUiEvent.LocationChanged(location)
                        )
                    }
                )
            }

            item {
                ScheduleRepeatSection(
                    repeatType = uiState.repeatType,
                    onRepeatTypeChange = { repeatType ->
                        onEvent(
                            ScheduleEditorUiEvent.RepeatTypeChanged(
                                repeatType
                            )
                        )
                    }
                )
            }

            item {
                ScheduleMemoField(
                    memo = uiState.memo,
                    onMemoChange = { memo ->
                        onEvent(
                            ScheduleEditorUiEvent.MemoChanged(memo)
                        )
                    }
                )
            }

            item {
                ScheduleSaveButton(
                    isEnabled = uiState.canSave,
                    onClick = {
                        onEvent(
                            ScheduleEditorUiEvent.SaveClicked
                        )
                    }
                )
            }
        }
    }
}

/**
 * 일정 등록 화면 상단 헤더다.
 */
@Composable
private fun ScheduleEditorHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(EditorHeaderBackgroundColor)
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "‹",
            modifier =
                Modifier
                    .clickable(
                        onClick = onBackClick
                    ).padding(
                        end = 16.dp
                    ),
            color = EditorBackButtonColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 34.sp
        )

        Text(
            text = "일정 등록",
            color = EditorHeaderTitleColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 일정 저장 버튼이다.
 */
@Composable
private fun ScheduleSaveButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color =
                        if (isEnabled) {
                            SaveButtonEnabledColor
                        } else {
                            SaveButtonDisabledColor
                        },
                    shape = RoundedCornerShape(16.dp)
                ).clickable(
                    enabled = isEnabled,
                    onClick = onClick
                ).padding(
                    vertical = 16.dp
                ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "저장",
            color = SaveButtonTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
