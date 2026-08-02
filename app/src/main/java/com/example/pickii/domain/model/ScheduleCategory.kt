package com.example.pickii.domain.model

/**
 * 일정을 분류하기 위한 태그 정보다.
 *
 * 화면에서는 개인, 알바, 약속 등의 이름으로 표시된다.
 *
 * @property id 태그 고유 ID
 * @property name 태그 이름
 * @property color 태그 색상
 */
data class ScheduleCategory(
    val id: Long,
    val name: String,
    val color: ScheduleColorType
)
