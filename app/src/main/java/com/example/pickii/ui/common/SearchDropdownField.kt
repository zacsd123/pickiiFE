package com.example.pickii.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray

/** 검색 필드의 모서리 둥글기. */
private val SearchFieldCornerRadius = 14.dp

/**
 * 검색어를 입력하면 아래에 후보 목록을 드롭다운으로 보여주는 검색형 입력 필드.
 * 학교 검색처럼 마스터 데이터를 자동완성으로 고르는 화면에서 재사용한다.
 *
 * @param query 현재 입력된 검색어
 * @param onQueryChange 검색어 변경 콜백
 * @param suggestions 검색어에 매칭되는 후보 목록(비어 있으면 드롭다운을 보여주지 않는다)
 * @param onSelect 후보 선택 콜백
 * @param itemLabel 후보 하나를 화면에 표시할 문구로 변환
 * @param placeholder 입력 필드 placeholder
 */
@Composable
fun <T> SearchDropdownField(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<T>,
    onSelect: (T) -> Unit,
    itemLabel: (T) -> String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var menuVisible by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                onQueryChange(newQuery)
                if (isFocused) menuVisible = true
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        menuVisible = focusState.isFocused
                    },
            placeholder = { Text(text = placeholder, color = PickiiTextGray) },
            singleLine = true,
            shape = RoundedCornerShape(SearchFieldCornerRadius),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PickiiFieldBackground,
                    unfocusedContainerColor = PickiiFieldBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
        )

        DropdownMenu(
            expanded = menuVisible && suggestions.isNotEmpty(),
            onDismissRequest = { menuVisible = false },
            properties = PopupProperties(focusable = false)
        ) {
            suggestions.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = itemLabel(item)) },
                    onClick = {
                        onSelect(item)
                        menuVisible = false
                    }
                )
            }
        }
    }
}
