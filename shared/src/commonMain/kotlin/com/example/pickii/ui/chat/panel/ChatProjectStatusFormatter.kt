package com.example.pickii.ui.chat

import com.example.pickii.domain.model.ProjectStatus

/**
 * 프로젝트 상태를 화면 표시용 문구로 변환한다.
 */
fun ProjectStatus.toDisplayText(): String =
    when (this) {
        ProjectStatus.IN_PROGRESS -> "진행 중"
        ProjectStatus.END -> "종료"
    }
