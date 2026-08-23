package com.example.pickii.domain.model

/** 사용자가 입력한 사용 가능 Skill/Tool 한 항목. [level]은 1(하)~3(상). */
data class SkillToolEntry(
    val techStackName: String,
    val level: Int
)
