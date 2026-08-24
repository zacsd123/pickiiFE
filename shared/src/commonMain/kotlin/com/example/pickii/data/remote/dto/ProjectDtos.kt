package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /projects/{projectId}`(6-2) 응답. 클라이언트는 `leaderId`만 쓴다(제목/기간/상태는 채팅방 상세로 이미 알고 있음). */
@Serializable
data class ProjectDetailDto(
    val leaderId: Long
)
