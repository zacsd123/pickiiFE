package com.example.pickii.domain.model

/** 모집 글 주제. 일부 주제는 아직 지원하지 않아 [isEnabled]가 false다. */
enum class RecruitTopic(
    val label: String,
    val isEnabled: Boolean = true
) {
    PLANNING("기획"),
    DESIGN("디자인"),
    RESEARCH("학술 연구"),
    IT("IT 개발"),
    VIDEO("영상 콘텐츠", isEnabled = false),
    ETC("기타", isEnabled = false)
}
