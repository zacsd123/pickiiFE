package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.AiDraftRequest
import com.example.pickii.data.remote.dto.ApplyAiDraftRequest
import com.example.pickii.data.remote.dto.ApplyRequest
import com.example.pickii.data.remote.dto.CommentCreateRequest
import com.example.pickii.data.remote.dto.ProjectCreateRequest
import com.example.pickii.data.remote.dto.RecruitWriteRequest
import io.ktor.client.statement.HttpResponse

/** `2. Main (Home)`, `3. Recruit` 문서의 엔드포인트. */
interface RecruitApiService {
    suspend fun getRecruits(
        keyword: String?,
        onCampus: Boolean?,
        categoryIds: List<Int>,
        topicIds: List<Int>,
        page: Int,
        size: Int
    ): HttpResponse

    suspend fun getRecruit(recruitId: Long): HttpResponse

    suspend fun createRecruit(request: RecruitWriteRequest): HttpResponse

    suspend fun updateRecruit(
        recruitId: Long,
        request: RecruitWriteRequest
    ): HttpResponse

    suspend fun generateRecruitAiDraft(request: AiDraftRequest): HttpResponse

    suspend fun closeRecruit(recruitId: Long): HttpResponse

    suspend fun reopenAdditionalRecruit(recruitId: Long): HttpResponse

    suspend fun deleteRecruit(recruitId: Long): HttpResponse

    suspend fun getComments(recruitId: Long): HttpResponse

    suspend fun createComment(
        recruitId: Long,
        request: CommentCreateRequest
    ): HttpResponse

    suspend fun deleteComment(commentId: Long): HttpResponse

    suspend fun generateApplyAiDraft(
        recruitId: Long,
        request: ApplyAiDraftRequest
    ): HttpResponse

    suspend fun submitApplication(
        recruitId: Long,
        request: ApplyRequest
    ): HttpResponse

    suspend fun scrapRecruit(recruitId: Long): HttpResponse

    suspend fun unscrapRecruit(recruitId: Long): HttpResponse

    /** 6-1 프로젝트 생성(그룹 채팅 생성). */
    suspend fun createProject(
        recruitId: Long,
        request: ProjectCreateRequest
    ): HttpResponse
}
