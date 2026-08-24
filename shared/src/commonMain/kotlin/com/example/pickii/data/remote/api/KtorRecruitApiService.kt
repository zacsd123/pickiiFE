package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.AiDraftRequest
import com.example.pickii.data.remote.dto.ApplyAiDraftRequest
import com.example.pickii.data.remote.dto.ApplyRequest
import com.example.pickii.data.remote.dto.CommentCreateRequest
import com.example.pickii.data.remote.dto.ProjectCreateRequest
import com.example.pickii.data.remote.dto.RecruitWriteRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [RecruitApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorRecruitApiService(
    private val client: HttpClient
) : RecruitApiService {
    override suspend fun getRecruits(
        keyword: String?,
        onCampus: Boolean?,
        categoryIds: List<Int>,
        topicIds: List<Int>,
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("recruits") {
            parameter("keyword", keyword)
            parameter("onCampus", onCampus)
            // Retrofit의 @Query List<Int>는 같은 키를 반복해서 보낸다(categoryIds=1&categoryIds=2).
            // Ktor의 parameter()는 값 하나만 toString()하므로, 같은 동작을 내려면 항목마다 호출해야 한다.
            categoryIds.forEach { parameter("categoryIds", it) }
            topicIds.forEach { parameter("topicIds", it) }
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun getRecruit(recruitId: Long): HttpResponse = client.get("recruits/$recruitId")

    override suspend fun createRecruit(request: RecruitWriteRequest): HttpResponse =
        client.post("recruits") {
            setBody(request)
        }

    override suspend fun updateRecruit(
        recruitId: Long,
        request: RecruitWriteRequest
    ): HttpResponse =
        client.patch("recruits/$recruitId") {
            setBody(request)
        }

    override suspend fun generateRecruitAiDraft(request: AiDraftRequest): HttpResponse =
        client.post("recruits/ai-draft") {
            setBody(request)
        }

    override suspend fun closeRecruit(recruitId: Long): HttpResponse = client.patch("recruits/$recruitId/close")

    override suspend fun reopenAdditionalRecruit(recruitId: Long): HttpResponse = client.patch("recruits/$recruitId/additional")

    override suspend fun deleteRecruit(recruitId: Long): HttpResponse = client.delete("recruits/$recruitId")

    override suspend fun getComments(recruitId: Long): HttpResponse = client.get("recruits/$recruitId/comments")

    override suspend fun createComment(
        recruitId: Long,
        request: CommentCreateRequest
    ): HttpResponse =
        client.post("recruits/$recruitId/comments") {
            setBody(request)
        }

    override suspend fun deleteComment(commentId: Long): HttpResponse = client.delete("comments/$commentId")

    override suspend fun generateApplyAiDraft(
        recruitId: Long,
        request: ApplyAiDraftRequest
    ): HttpResponse =
        client.post("recruits/$recruitId/applies/ai-draft") {
            setBody(request)
        }

    override suspend fun submitApplication(
        recruitId: Long,
        request: ApplyRequest
    ): HttpResponse =
        client.post("recruits/$recruitId/applies") {
            setBody(request)
        }

    override suspend fun scrapRecruit(recruitId: Long): HttpResponse = client.post("recruits/$recruitId/scrap")

    override suspend fun unscrapRecruit(recruitId: Long): HttpResponse = client.delete("recruits/$recruitId/scrap")

    override suspend fun createProject(
        recruitId: Long,
        request: ProjectCreateRequest
    ): HttpResponse =
        client.post("recruits/$recruitId/project") {
            setBody(request)
        }
}
