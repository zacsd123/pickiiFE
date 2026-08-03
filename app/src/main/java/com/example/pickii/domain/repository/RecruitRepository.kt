package com.example.pickii.domain.repository

import com.example.pickii.domain.model.RecruitApplication
import com.example.pickii.domain.model.RecruitComment
import com.example.pickii.domain.model.RecruitPost
import kotlinx.coroutines.flow.StateFlow

/** 모집 글, 댓글, 스크랩, 지원, AI 초안 생성을 담당한다. */
interface RecruitRepository {
    /** 전체 모집 글 목록을 관찰한다. */
    fun observePosts(): StateFlow<List<RecruitPost>>

    /** [postId]에 해당하는 모집 글을 찾는다. 없으면 null이다. */
    fun getPostById(postId: String): RecruitPost?

    /** 새 모집 글을 등록한다. */
    suspend fun createPost(post: RecruitPost): Result<RecruitPost>

    /** 기존 모집 글을 수정한다. */
    suspend fun updatePost(post: RecruitPost): Result<RecruitPost>

    /** 모집 글을 마감(CLOSED) 상태로 바꾼다. */
    suspend fun closePost(postId: String): Result<Unit>

    /** 모집 글을 삭제한다. */
    suspend fun deletePost(postId: String): Result<Unit>

    /** [postId] 모집 글에 달린 댓글/답글 전체를 관찰한다. */
    fun observeComments(postId: String): StateFlow<List<RecruitComment>>

    /**
     * 댓글 또는 답글을 작성한다.
     *
     * @param parentCommentId 답글이면 상위 댓글 id, 최상위 댓글이면 null
     */
    suspend fun addComment(
        postId: String,
        authorId: String,
        authorNickname: String,
        parentCommentId: String?,
        content: String
    ): Result<RecruitComment>

    /** 댓글을 soft delete한다(내용은 유지하고 삭제 표시만 한다). */
    suspend fun deleteComment(commentId: String): Result<Unit>

    /** [userId]가 [postId]를 스크랩했는지 여부. */
    fun isScraped(
        postId: String,
        userId: String
    ): Boolean

    /** 스크랩 여부를 토글하고, 토글 후 상태를 반환한다. */
    suspend fun toggleScrap(
        postId: String,
        userId: String
    ): Result<Boolean>

    /** [userId]가 [postId]에 이미 지원했는지 여부. */
    fun hasApplied(
        postId: String,
        userId: String
    ): Boolean

    /** 모집 글에 지원한다. */
    suspend fun submitApplication(
        postId: String,
        applicantId: String,
        applicantNickname: String,
        message: String
    ): Result<RecruitApplication>

    /**
     * AI 초안을 생성한다. 백엔드가 없어 목업으로 동작하며, 일정 호출마다 결정적으로 실패한다.
     *
     * @param prompt 초안 생성에 사용할 입력(제목/카테고리 등을 조합한 문자열)
     */
    suspend fun generateAiDraft(prompt: String): Result<String>
}
