package com.example.pickii.domain.repository

import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.ProjectCreationResult
import com.example.pickii.domain.model.RecruitComment
import com.example.pickii.domain.model.RecruitPage
import com.example.pickii.domain.model.RecruitPost
import java.time.LocalDate

/** 모집 글, 댓글, 스크랩, 지원, AI 초안 생성을 담당한다. */
interface RecruitRepository {
    /** 검색/필터 조건에 맞는 모집 글 목록을 한 페이지 조회한다. [page]는 0부터 시작한다. */
    suspend fun getPosts(
        keyword: String? = null,
        onCampus: Boolean? = null,
        categoryIds: List<Int> = emptyList(),
        topicIds: List<Int> = emptyList(),
        page: Int = 0,
        size: Int = 10
    ): Result<RecruitPage>

    /** [postId]에 해당하는 모집 글 상세를 조회한다. */
    suspend fun getPostById(postId: String): Result<RecruitPost>

    /** 새 모집 글을 등록하고, 성공하면 새로 생성된 글의 id를 반환한다. */
    suspend fun createPost(
        title: String,
        maxParticipants: Int,
        onCampus: CampusScope,
        categoryIds: List<Int>,
        topicIds: List<Int>,
        startDate: LocalDate,
        endDate: LocalDate,
        shortIntro: String,
        detailContent: String
    ): Result<String>

    /** 기존 모집 글을 수정한다. */
    suspend fun updatePost(
        postId: String,
        title: String,
        maxParticipants: Int,
        onCampus: CampusScope,
        categoryIds: List<Int>,
        topicIds: List<Int>,
        startDate: LocalDate,
        endDate: LocalDate,
        shortIntro: String,
        detailContent: String
    ): Result<Unit>

    /** 모집 글을 마감(CLOSED) 상태로 바꾼다. */
    suspend fun closePost(postId: String): Result<Unit>

    /** 마감된 모집 글을 추가 모집(ADDITIONAL) 상태로 바꾼다. */
    suspend fun reopenAdditionalRecruiting(postId: String): Result<Unit>

    /** 모집 글을 삭제한다. */
    suspend fun deletePost(postId: String): Result<Unit>

    /** [postId] 모집 글에 달린 댓글/답글 전체를 조회한다(계층 구조가 평탄화되지 않은 트리 그대로). */
    suspend fun getComments(postId: String): Result<List<RecruitComment>>

    /**
     * 댓글 또는 답글을 작성하고, 성공하면 새로 생성된 댓글의 id를 반환한다.
     *
     * @param parentCommentId 답글이면 상위 댓글 id, 최상위 댓글이면 null
     */
    suspend fun addComment(
        postId: String,
        parentCommentId: String?,
        content: String
    ): Result<String>

    /** 댓글을 soft delete한다(내용은 유지하고 삭제 표시만 한다). */
    suspend fun deleteComment(commentId: String): Result<Unit>

    /** 모집 글을 스크랩하고, 성공하면 true를 반환한다. */
    suspend fun scrapPost(postId: String): Result<Boolean>

    /** 모집 글 스크랩을 취소한다. */
    suspend fun unscrapPost(postId: String): Result<Unit>

    /** 모집 글에 지원한다. */
    suspend fun submitApplication(
        postId: String,
        message: String
    ): Result<Unit>

    /**
     * 공고 간단 소개/상세 내용 초안을 AI로 다듬는다. [content]는 필수(공란 불가)다.
     *
     * @return (간단 소개, 상세 내용) 쌍
     */
    suspend fun generateAiDraft(
        simpleDesc: String?,
        content: String
    ): Result<Pair<String, String>>

    /** 지원 메시지 초안을 공고 맥락에 맞게 AI로 다듬는다. */
    suspend fun generateApplyAiDraft(
        postId: String,
        message: String
    ): Result<String>

    /**
     * 공고를 프로젝트(그룹 채팅)로 전환한다(6-1). 수락된(ACCEPTED) 지원자가 1명 이상 있어야 하며,
     * 이미 프로젝트가 생성된 공고면 실패한다.
     */
    suspend fun createProject(
        postId: String,
        name: String
    ): Result<ProjectCreationResult>
}
