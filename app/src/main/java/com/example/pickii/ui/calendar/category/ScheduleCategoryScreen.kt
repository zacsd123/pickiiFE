package com.example.pickii.ui.calendar.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.domain.model.ScheduleColorType

private val ScreenBackgroundColor = Color(0xFFFFFFFF)
private val ItemBackgroundColor = Color(0xFFF4F4F4)
private val InputBackgroundColor = Color(0xFFF4F4F4)
private val PrimaryTextColor = Color(0xFF1B1B1B)
private val SecondaryTextColor = Color(0xFF8C8C8C)
private val AddButtonColor = Color(0xFFE8FF7A)
private val SaveButtonColor = Color(0xFF111111)

/**
 * 태그 관리 화면이다.
 */
@Composable
fun ScheduleCategoryScreen(
    uiState: ScheduleCategoryUiState,
    onCloseClick: () -> Unit,
    onCategoryEditClick: (Long) -> Unit,
    onCategoryDeleteClick: (Long) -> Unit,
    onCategoryNameChange: (String) -> Unit,
    onColorSelect: (ScheduleColorType) -> Unit,
    onAddCategoryClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackgroundColor),
    ) {
        ScheduleCategoryHeader(
            onCloseClick = onCloseClick,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = uiState.categories,
                key = { category -> category.id },
            ) { category ->
                ScheduleCategoryItem(
                    category = category,
                    onEditClick = {
                        onCategoryEditClick(category.id)
                    },
                    onDeleteClick = {
                        onCategoryDeleteClick(category.id)
                    },
                )
            }

            item {
                Spacer(
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            item {
                Text(
                    text = "새 태그 추가",
                    color = SecondaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            item {
                BasicTextField(
                    value = uiState.newCategoryName,
                    onValueChange = onCategoryNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = InputBackgroundColor,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 14.dp,
                        ),
                    textStyle = TextStyle(
                        color = PrimaryTextColor,
                        fontSize = 16.sp,
                    ),
                    cursorBrush = SolidColor(PrimaryTextColor),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (uiState.newCategoryName.isBlank()) {
                            Text(
                                text = "태그 이름",
                                color = SecondaryTextColor,
                                fontSize = 16.sp,
                            )
                        }

                        innerTextField()
                    },
                )
            }

            item {
                ScheduleColorPalette(
                    selectedColor = uiState.selectedColor,
                    onColorSelect = onColorSelect,
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = AddButtonColor,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clickable(
                            enabled = uiState.canAddCategory,
                            onClick = onAddCategoryClick,
                        )
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.editingCategoryId == null) {
                            "태그 추가"
                        } else {
                            "태그 수정"
                        },
                        color = PrimaryTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp,
                )
                .background(
                    color = SaveButtonColor,
                    shape = RoundedCornerShape(28.dp),
                )
                .clickable(onClick = onSaveClick)
                .padding(vertical = 17.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "저장",
                color = AddButtonColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ScheduleCategoryHeader(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "태그 관리",
            color = PrimaryTextColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "×",
            modifier = Modifier
                .clickable(onClick = onCloseClick)
                .padding(4.dp),
            color = PrimaryTextColor,
            fontSize = 28.sp,
        )
    }
}

@Composable
private fun ScheduleCategoryItem(
    category: ScheduleCategory,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ItemBackgroundColor,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = category.color.toComposeColor(),
                    shape = CircleShape,
                ),
        )

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Text(
            text = category.name,
            modifier = Modifier.weight(1f),
            color = PrimaryTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )

        Text(
            text = "✎",
            modifier = Modifier
                .clickable(onClick = onEditClick)
                .padding(6.dp),
            color = SecondaryTextColor,
            fontSize = 18.sp,
        )

        Text(
            text = "⌫",
            modifier = Modifier
                .clickable(onClick = onDeleteClick)
                .padding(6.dp),
            color = Color(0xFFFF6B6B),
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun ScheduleColorPalette(
    selectedColor: ScheduleColorType,
    onColorSelect: (ScheduleColorType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScheduleColorType.entries
            .filterNot { color ->
                color == ScheduleColorType.BLACK
            }
            .forEach { color ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            color = color.toComposeColor(),
                            shape = CircleShape,
                        )
                        .clickable {
                            onColorSelect(color)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (color == selectedColor) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
    }
}

private fun ScheduleColorType.toComposeColor(): Color {
    return when (this) {
        ScheduleColorType.RED -> Color(0xFFE95F5F)
        ScheduleColorType.ORANGE -> Color(0xFFEB9852)
        ScheduleColorType.YELLOW -> Color(0xFFF2CF4A)
        ScheduleColorType.GREEN -> Color(0xFF73C86B)
        ScheduleColorType.BLUE -> Color(0xFF6699EC)
        ScheduleColorType.PURPLE -> Color(0xFF9271EA)
        ScheduleColorType.PINK -> Color(0xFFDE7FA4)
        ScheduleColorType.GRAY -> Color(0xFF8F959D)
        ScheduleColorType.BLACK -> Color(0xFF1B1B1B)
    }
}