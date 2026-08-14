package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `{ "data": ..., "timestamp": ... }` 형태의 공통 성공 응답 래퍼. */
@Serializable
data class ApiEnvelope<T>(
    val data: T,
    val timestamp: String
)

/** `{ "data": { "content": [...], "pageInfo": {...} }, "timestamp": ... }` 형태의 페이지네이션 응답. */
@Serializable
data class PageEnvelope<T>(
    val content: List<T>,
    val pageInfo: PageInfo
)

@Serializable
data class PageInfo(
    val currentPage: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean
)

/** `{ "error": { "code": ..., "message": ... }, "timestamp": ... }` 형태의 공통 에러 응답. */
@Serializable
data class ApiErrorBody(
    val error: ApiError,
    val timestamp: String
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

/** 서버 에러 응답의 [ApiError.code]/[ApiError.message]를 그대로 담는 예외. */
class ApiException(
    val code: String,
    message: String
) : Exception(message)
