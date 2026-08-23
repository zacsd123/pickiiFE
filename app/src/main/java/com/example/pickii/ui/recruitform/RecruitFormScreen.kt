package com.example.pickii.ui.recruitform

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.ui.common.AiGenerationDialog
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.CampusScopeToggle
import com.example.pickii.ui.common.CharacterCounterText
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.FieldLabel
import com.example.pickii.ui.common.LoginRequiredDialog
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.common.SelectableChip
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellow
import com.example.pickii.util.toDisplayString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 입력 필드/버튼에 공통으로 사용하는 모서리 둥글기. */
private val FieldCornerRadius = 14.dp

/** 등록/수정 버튼의 높이. */
private val ActionButtonHeight = 52.dp

/**
 * 공고 글 등록/수정 화면. 라우팅에 postId가 있으면 수정 모드, 없으면 등록 모드로 동작한다.
 *
 * [RecruitFormViewModel]에서 입력 상태를 받아와 [RecruitFormScreenContent]에 전달한다.
 *
 * @param onBackClick 뒤로가기 클릭 콜백
 * @param onSubmitComplete 등록/수정이 최종적으로 완료되어 화면을 벗어나야 할 때 호출되는 콜백
 * @param onNavigateToLogin 비로그인 상태에서 "로그인하러 가기"를 눌렀을 때 호출되는 콜백
 */
@Composable
fun RecruitFormScreen(
    onBackClick: () -> Unit = {},
    onSubmitComplete: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: RecruitFormViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                Toast
                    .makeText(
                        context,
                        context.getString(event.messageRes),
                        Toast.LENGTH_SHORT
                    ).show()
        }
    }

    RecruitFormScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onNavigateToLogin = onNavigateToLogin,
        onTitleChange = viewModel::onTitleChange,
        onMaxParticipantsChange = viewModel::onMaxParticipantsChange,
        onCampusScopeChange = viewModel::onCampusScopeChange,
        onCategorySelect = viewModel::onCategorySelect,
        onTopicSelect = viewModel::onTopicSelect,
        onStartDateChange = viewModel::onStartDateChange,
        onEndDateChange = viewModel::onEndDateChange,
        onShortIntroChange = viewModel::onShortIntroChange,
        onDetailContentChange = viewModel::onDetailContentChange,
        onGenerateAiDraftClick = viewModel::onGenerateAiDraftClick,
        onDismissAiDialog = viewModel::onDismissAiDialog,
        onSubmitClick = viewModel::onSubmitClick,
        onDismissSubmitConfirm = viewModel::onDismissSubmitConfirm,
        onConfirmSubmit = { viewModel.onConfirmSubmit(onEditComplete = onSubmitComplete) },
        onCompleteDialogConfirm = { viewModel.onCompleteDialogConfirm(onComplete = onSubmitComplete) }
    )
}

/** [RecruitFormScreen]의 실제 UI. ViewModel 없이도 미리보기가 가능하도록 상태를 파라미터로 받는다. */
@Composable
private fun RecruitFormScreenContent(
    uiState: RecruitFormUiState,
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onTitleChange: (String) -> Unit,
    onMaxParticipantsChange: (Int) -> Unit,
    onCampusScopeChange: (CampusScope) -> Unit,
    onCategorySelect: (RecruitCategory) -> Unit,
    onTopicSelect: (RecruitTopic) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onShortIntroChange: (String) -> Unit,
    onDetailContentChange: (String) -> Unit,
    onGenerateAiDraftClick: () -> Unit,
    onDismissAiDialog: () -> Unit,
    onSubmitClick: () -> Unit,
    onDismissSubmitConfirm: () -> Unit,
    onConfirmSubmit: () -> Unit,
    onCompleteDialogConfirm: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
    ) {
        FormTopBar(
            title =
                stringResource(
                    if (uiState.mode ==
                        RecruitFormMode.CREATE
                    ) {
                        R.string.recruit_form_title_create
                    } else {
                        R.string.recruit_form_title_edit
                    }
                ),
            onBackClick = onBackClick
        )

        when {
            uiState.isLoginRequired -> LoginRequiredDialog(onLoginClick = onNavigateToLogin, onDismiss = onBackClick)
            !uiState.isAuthorized -> NoAccessMessage()
            else ->
                FormBody(
                    uiState = uiState,
                    onTitleChange = onTitleChange,
                    onMaxParticipantsChange = onMaxParticipantsChange,
                    onCampusScopeChange = onCampusScopeChange,
                    onCategorySelect = onCategorySelect,
                    onTopicSelect = onTopicSelect,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange,
                    onShortIntroChange = onShortIntroChange,
                    onDetailContentChange = onDetailContentChange,
                    onGenerateAiDraftClick = onGenerateAiDraftClick,
                    onDismissAiDialog = onDismissAiDialog,
                    onSubmitClick = onSubmitClick,
                    onDismissSubmitConfirm = onDismissSubmitConfirm,
                    onConfirmSubmit = onConfirmSubmit,
                    onCompleteDialogConfirm = onCompleteDialogConfirm
                )
        }
    }
}

/** 뒤로가기 버튼과 화면 제목을 보여주는 상단 바. */
@Composable
private fun FormTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    BackHeader(
        title = title,
        onBackClick = onBackClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        titleFontSize = 20.sp
    )
}

/** 수정 화면에 작성자가 아닌 사용자가 접근했을 때 표시되는 안내 문구. */
@Composable
private fun NoAccessMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.recruit_form_error_no_access),
            color = PickiiTextGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 공고 제목/모집 인원/교내 구분/카테고리/주제/기간/소개/상세 사항을 입력받는 폼 본문. */
@Composable
private fun FormBody(
    uiState: RecruitFormUiState,
    onTitleChange: (String) -> Unit,
    onMaxParticipantsChange: (Int) -> Unit,
    onCampusScopeChange: (CampusScope) -> Unit,
    onCategorySelect: (RecruitCategory) -> Unit,
    onTopicSelect: (RecruitTopic) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onShortIntroChange: (String) -> Unit,
    onDetailContentChange: (String) -> Unit,
    onGenerateAiDraftClick: () -> Unit,
    onDismissAiDialog: () -> Unit,
    onSubmitClick: () -> Unit,
    onDismissSubmitConfirm: () -> Unit,
    onConfirmSubmit: () -> Unit,
    onCompleteDialogConfirm: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
    ) {
        FieldSectionLabel(text = stringResource(R.string.recruit_form_label_title))
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.recruit_form_placeholder_title),
                    color = PickiiTextGray
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(FieldCornerRadius),
            colors = pickiiFieldColors()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (!uiState.isTitleValid) {
                Text(
                    text = stringResource(R.string.recruit_form_helper_title_min_length),
                    color = PickiiTextGray,
                    fontSize = 11.sp
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            CharacterCounterText(current = uiState.title.length, max = MAX_TITLE_LENGTH)
        }

        Spacer(modifier = Modifier.height(20.dp))

        FieldSectionLabel(text = stringResource(R.string.recruit_form_label_capacity))
        ParticipantsDropdown(selected = uiState.maxParticipants, onSelect = onMaxParticipantsChange)

        Spacer(modifier = Modifier.height(20.dp))

        FieldSectionLabel(text = stringResource(R.string.recruit_form_label_campus_scope))
        CampusScopeToggle(selected = uiState.onCampus ?: CampusScope.INTERNAL, onSelect = onCampusScopeChange)

        Spacer(modifier = Modifier.height(20.dp))

        FieldSectionLabel(text = stringResource(R.string.recruit_form_label_category_and_topic))

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.recruit_form_label_category), color = PickiiTextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.availableCategories.forEach { category ->
                SelectableChip(
                    label = category.label,
                    selected = category in uiState.categories,
                    enabled = true,
                    onClick = { onCategorySelect(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.recruit_form_label_topic), color = PickiiTextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.availableTopics.forEach { topic ->
                SelectableChip(
                    label = topic.label,
                    selected = topic in uiState.topics,
                    enabled = true,
                    onClick = { onTopicSelect(topic) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FieldSectionLabel(text = stringResource(R.string.recruit_form_label_period))
        DateRangeRow(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            onStartDateChange = onStartDateChange,
            onEndDateChange = onEndDateChange
        )
        Text(text = stringResource(R.string.recruit_form_helper_period), color = PickiiTextGray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(20.dp))

        FieldSectionLabel(text = stringResource(R.string.recruit_form_label_short_intro))
        OutlinedTextField(
            value = uiState.shortIntro,
            onValueChange = onShortIntroChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(90.dp),
            placeholder = {
                val categoryLabel =
                    uiState.categories.firstOrNull()?.label ?: stringResource(R.string.recruit_form_label_category)
                Text(
                    text = stringResource(R.string.recruit_form_placeholder_short_intro, categoryLabel),
                    color = PickiiTextGray
                )
            },
            shape = RoundedCornerShape(FieldCornerRadius),
            colors = pickiiFieldColors()
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            CharacterCounterText(current = uiState.shortIntro.length, max = MAX_SHORT_INTRO_LENGTH)
        }

        Spacer(modifier = Modifier.height(20.dp))

        FieldSectionLabel(text = stringResource(R.string.recruit_form_label_detail_content))
        OutlinedTextField(
            value = uiState.detailContent,
            onValueChange = onDetailContentChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.recruit_form_placeholder_detail_content),
                    color = PickiiTextGray
                )
            },
            shape = RoundedCornerShape(FieldCornerRadius),
            colors = pickiiFieldColors()
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            CharacterCounterText(current = uiState.detailContent.length, max = MAX_DETAIL_LENGTH)
        }

        Spacer(modifier = Modifier.height(16.dp))

        AiDraftBanner(onClick = onGenerateAiDraftClick)

        Spacer(modifier = Modifier.height(20.dp))

        SubmitButton(
            label =
                stringResource(
                    if (uiState.mode ==
                        RecruitFormMode.CREATE
                    ) {
                        R.string.recruit_form_button_submit_create
                    } else {
                        R.string.recruit_form_button_submit_edit
                    }
                ),
            enabled = uiState.isSubmitEnabled,
            onClick = onSubmitClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    AiGenerationDialog(state = uiState.aiDialogState, onRetry = onGenerateAiDraftClick, onDismiss = onDismissAiDialog)

    if (uiState.isSubmitConfirmDialogVisible) {
        ConfirmDialog(
            title =
                stringResource(
                    if (uiState.mode == RecruitFormMode.CREATE) {
                        R.string.recruit_form_dialog_submit_create_title
                    } else {
                        R.string.recruit_form_dialog_submit_edit_title
                    }
                ),
            body = stringResource(R.string.recruit_form_dialog_submit_body),
            confirmLabel = stringResource(R.string.common_button_confirm),
            dismissLabel = stringResource(R.string.common_button_cancel),
            onConfirm = onConfirmSubmit,
            onDismiss = onDismissSubmitConfirm
        )
    }

    if (uiState.isCompleteDialogVisible) {
        CreateCompleteDialog(onConfirm = onCompleteDialogConfirm)
    }
}

/** 공고 글 등록 완료를 알리는 단일 확인 버튼 팝업. */
@Composable
private fun CreateCompleteDialog(onConfirm: () -> Unit) {
    ConfirmDialog(
        title = stringResource(R.string.recruit_form_dialog_create_complete_title),
        body = stringResource(R.string.recruit_form_dialog_create_complete_body),
        confirmLabel = stringResource(R.string.common_button_confirm),
        onConfirm = onConfirm
    )
}

/** 입력 필드 위에 표시되는 굵은 섹션 라벨. */
@Composable
private fun FieldSectionLabel(text: String) {
    FieldLabel(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
}

/** 이 화면의 입력 필드들이 공통으로 사용하는 배경/테두리 색상. */
@Composable
private fun pickiiFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = PickiiFieldBackground,
        unfocusedContainerColor = PickiiFieldBackground,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent
    )

/** 1명부터 최대 인원까지 고를 수 있는 모집 인원 드롭다운. */
@Composable
private fun ParticipantsDropdown(
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val unit = stringResource(R.string.recruit_form_placeholder_capacity_unit)

    Box {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(FieldCornerRadius))
                    .background(PickiiFieldBackground)
                    .clickable { isExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = selected?.let { "$it$unit" } ?: unit,
                color = if (selected != null) Color.Black else PickiiTextGray,
                fontSize = 14.sp
            )
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            (MIN_PARTICIPANTS..MAX_PARTICIPANTS).forEach { count ->
                DropdownMenuItem(
                    text = { Text(text = "$count$unit") },
                    onClick = {
                        onSelect(count)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

/** 진행 기간의 시작일/종료일 중 어느 쪽을 고르는 중인지 구분한다. */
private enum class DateField { START, END }

/** 시작일/종료일을 나란히 보여주고, 클릭 시 [DatePickerDialog]로 날짜를 고르게 한다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeRow(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit
) {
    var openDatePickerFor by remember { mutableStateOf<DateField?>(null) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        DateBox(date = startDate, onClick = { openDatePickerFor = DateField.START }, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "~", color = PickiiTextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        DateBox(date = endDate, onClick = { openDatePickerFor = DateField.END }, modifier = Modifier.weight(1f))
    }

    val target = openDatePickerFor
    if (target != null) {
        val initialDate = if (target == DateField.START) startDate else endDate
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate?.toEpochMillisUtc())

        DatePickerDialog(
            onDismissRequest = { openDatePickerFor = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateUtc()?.let { pickedDate ->
                            if (target ==
                                DateField.START
                            ) {
                                onStartDateChange(pickedDate)
                            } else {
                                onEndDateChange(pickedDate)
                            }
                        }
                        openDatePickerFor = null
                    }
                ) {
                    Text(text = stringResource(R.string.common_button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { openDatePickerFor = null }) {
                    Text(text = stringResource(R.string.common_button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** 날짜 하나를 보여주는 클릭 가능한 상자. 아직 고르지 않았으면 안내 문구를 보여준다. */
@Composable
private fun DateBox(
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(FieldCornerRadius))
                .background(PickiiFieldBackground)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = date?.toDisplayString() ?: stringResource(R.string.recruit_form_placeholder_date),
            color = if (date != null) Color.Black else PickiiTextGray,
            fontSize = 13.sp
        )
    }
}

/** 이 날짜의 UTC 자정 시각을 epoch millisecond로 변환한다([DatePicker] 초기값 용도). */
@OptIn(ExperimentalTime::class)
private fun LocalDate.toEpochMillisUtc(): Long = atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()

/** [DatePicker]가 반환한 epoch millisecond를 UTC 기준 날짜로 변환한다. */
@OptIn(ExperimentalTime::class)
private fun Long.toLocalDateUtc(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

/** AI 초안 생성을 안내하고 실행하는 배너. */
@Composable
private fun AiDraftBanner(onClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PickiiYellow)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.recruit_form_banner_ai_draft_title),
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.recruit_form_banner_ai_draft_subtitle),
                color = PickiiTextGray,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.recruit_form_button_ai_draft),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** 등록/수정 완료 버튼. 필수 항목이 채워지지 않으면 비활성화된다. */
@Composable
private fun SubmitButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ActionButtonHeight)
                .clip(RoundedCornerShape(FieldCornerRadius))
                .background(if (enabled) PickiiBlue else PickiiDisabledGray)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

/** [RecruitFormScreen]의 프리뷰(공고 글 등록, 초기 상태). */
@Preview(showBackground = true)
@Composable
private fun RecruitFormScreenCreatePreview() {
    MaterialTheme {
        RecruitFormScreenContent(
            uiState = RecruitFormUiState(mode = RecruitFormMode.CREATE),
            onBackClick = {},
            onNavigateToLogin = {},
            onTitleChange = {},
            onMaxParticipantsChange = {},
            onCampusScopeChange = {},
            onCategorySelect = {},
            onTopicSelect = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onShortIntroChange = {},
            onDetailContentChange = {},
            onGenerateAiDraftClick = {},
            onDismissAiDialog = {},
            onSubmitClick = {},
            onDismissSubmitConfirm = {},
            onConfirmSubmit = {},
            onCompleteDialogConfirm = {}
        )
    }
}

/** [RecruitFormScreen]의 프리뷰(공고 글 수정, 권한 없음 상태). */
@Preview(showBackground = true)
@Composable
private fun RecruitFormScreenNoAccessPreview() {
    MaterialTheme {
        RecruitFormScreenContent(
            uiState = RecruitFormUiState(mode = RecruitFormMode.EDIT, isAuthorized = false),
            onBackClick = {},
            onNavigateToLogin = {},
            onTitleChange = {},
            onMaxParticipantsChange = {},
            onCampusScopeChange = {},
            onCategorySelect = {},
            onTopicSelect = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onShortIntroChange = {},
            onDetailContentChange = {},
            onGenerateAiDraftClick = {},
            onDismissAiDialog = {},
            onSubmitClick = {},
            onDismissSubmitConfirm = {},
            onConfirmSubmit = {},
            onCompleteDialogConfirm = {}
        )
    }
}
