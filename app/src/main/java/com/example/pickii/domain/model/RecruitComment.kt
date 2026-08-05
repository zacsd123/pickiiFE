package com.example.pickii.domain.model

import java.time.LocalDateTime

/**
 * 공고 상세의 댓글 또는 답글 하나.
 *
 * [parentCommentId]가 null이면 최상위 댓글, 아니면 해당 댓글에 달린 답글이다.
 * 삭제된 댓글도 하위 답글의 [parentCommentId]를 유지하기 위해 목록에서 지우지 않고
 * [isDeleted]만 true로 바꾼다(soft delete).
 */
data class RecruitComment(
    val id: String,
    val postId: String,
    val parentCommentId: String?,
    val authorId: String,
    val authorNickname: String,
    val content: String,
    val createdAt: LocalDateTime,
    val isDeleted: Boolean = false,
    val isAuthor: Boolean = false
)
