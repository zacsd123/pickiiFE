package com.example.pickii.domain.model

/** 자격증 마스터 데이터(`GET /licenses`) 항목. 자동완성 선택지로만 쓰이고, 실제 저장은 이름 문자열로 한다. */
data class LicenseOption(
    val id: Int,
    val name: String
)
