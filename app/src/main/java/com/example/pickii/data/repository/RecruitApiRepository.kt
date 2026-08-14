package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.RecruitApiService
import com.example.pickii.data.remote.dto.AiDraftRequest
import com.example.pickii.data.remote.dto.ApplyAiDraftRequest
import com.example.pickii.data.remote.dto.ApplyRequest
import com.example.pickii.data.remote.dto.CommentCreateRequest
import com.example.pickii.data.remote.dto.CommentDto
import com.example.pickii.data.remote.dto.ProjectCreateRequest
import com.example.pickii.data.remote.dto.RecruitDetailDto
import com.example.pickii.data.remote.dto.RecruitSummaryDto
import com.example.pickii.data.remote.dto.RecruitWriteRequest
import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.ProjectCreationResult
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitComment
import com.example.pickii.domain.model.RecruitPage
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.model.RecruitPostSummary
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.example.pickii.util.parseIsoOffsetDateTime
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** 검색 키워드로 인정하는 최소 글자 수(0.7 공통 Validation 규칙). 더 짧으면 필터 없이 조회한다. */
private const val MIN_KEYWORD_LENGTH = 2

/** `2. Main (Home)`, `3. Recruit` API로 [RecruitRepository]를 구현한다. DTO ↔ 도메인 매핑을 전담한다. */
@Singleton
class RecruitApiRepository
    @Inject
    constructor(
        private val recruitApiService: RecruitApiService,
        private val masterDataRepository: MasterDataRepository,
        private val json: Json
    ) : RecruitRepository {
        override suspend fun getPosts(
            keyword: String?,
            onCampus: Boolean?,
            categoryIds: List<Int>,
            topicIds: List<Int>,
            page: Int,
            size: Int
        ): Result<RecruitPage> =
            safeApiCall(json) {
                recruitApiService.getRecruits(
                    keyword = keyword?.takeIf { it.length >= MIN_KEYWORD_LENGTH },
                    onCampus = onCampus,
                    categoryIds = categoryIds,
                    topicIds = topicIds,
                    page = page,
                    size = size
                )
            }.map { envelope ->
                val pageEnvelope = envelope.data
                RecruitPage(
                    posts = pageEnvelope.content.map { it.toDomain() },
                    currentPage = pageEnvelope.pageInfo.currentPage,
                    totalPages = pageEnvelope.pageInfo.totalPages,
                    totalElements = pageEnvelope.pageInfo.totalElements,
                    hasNext = pageEnvelope.pageInfo.hasNext
                )
            }

        override suspend fun getPostById(postId: String): Result<RecruitPost> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            val categories = masterDataRepository.getCategories().getOrDefault(emptyList())
            val topics = masterDataRepository.getTopics().getOrDefault(emptyList())
            return safeApiCall(json) { recruitApiService.getRecruit(id) }
                .map { it.data.toDomain(categories, topics) }
        }

        override suspend fun createPost(
            title: String,
            maxParticipants: Int,
            onCampus: CampusScope,
            categoryIds: List<Int>,
            topicIds: List<Int>,
            startDate: LocalDate,
            endDate: LocalDate,
            shortIntro: String,
            detailContent: String
        ): Result<String> =
            safeApiCall(json) {
                recruitApiService.createRecruit(
                    buildWriteRequest(
                        title = title,
                        maxParticipants = maxParticipants,
                        onCampus = onCampus,
                        categoryIds = categoryIds,
                        topicIds = topicIds,
                        startDate = startDate,
                        endDate = endDate,
                        shortIntro = shortIntro,
                        detailContent = detailContent
                    )
                )
            }.map { it.data.recruitId.toString() }

        override suspend fun updatePost(
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
        ): Result<Unit> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCallUnit(json) {
                recruitApiService.updateRecruit(
                    id,
                    buildWriteRequest(
                        title = title,
                        maxParticipants = maxParticipants,
                        onCampus = onCampus,
                        categoryIds = categoryIds,
                        topicIds = topicIds,
                        startDate = startDate,
                        endDate = endDate,
                        shortIntro = shortIntro,
                        detailContent = detailContent
                    )
                )
            }
        }

        override suspend fun closePost(postId: String): Result<Unit> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCallUnit(json) { recruitApiService.closeRecruit(id) }
        }

        override suspend fun reopenAdditionalRecruiting(postId: String): Result<Unit> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCallUnit(json) { recruitApiService.reopenAdditionalRecruit(id) }
        }

        override suspend fun deletePost(postId: String): Result<Unit> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCallUnit(json) { recruitApiService.deleteRecruit(id) }
        }

        override suspend fun getComments(postId: String): Result<List<RecruitComment>> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCall(json) { recruitApiService.getComments(id) }
                .map { envelope -> envelope.data.comments.flatMap { it.flatten(postId) } }
        }

        override suspend fun addComment(
            postId: String,
            parentCommentId: String?,
            content: String
        ): Result<String> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            val request = CommentCreateRequest(content = content, parentCommentId = parentCommentId?.toLongOrNull())
            return safeApiCall(json) { recruitApiService.createComment(id, request) }
                .map { it.data.commentId.toString() }
        }

        override suspend fun deleteComment(commentId: String): Result<Unit> {
            val id = commentId.toLongOrNull() ?: return Result.failure(invalidPostIdException(commentId))
            return safeApiCallUnit(json) { recruitApiService.deleteComment(id) }
        }

        override suspend fun scrapPost(postId: String): Result<Boolean> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCall(json) { recruitApiService.scrapRecruit(id) }.map { it.data.isScrapped }
        }

        override suspend fun unscrapPost(postId: String): Result<Unit> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCallUnit(json) { recruitApiService.unscrapRecruit(id) }
        }

        override suspend fun submitApplication(
            postId: String,
            message: String,
            keywordIds: List<Long>
        ): Result<Unit> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCallUnit(json) {
                recruitApiService.submitApplication(
                    id,
                    ApplyRequest(message = message, keywordIds = keywordIds.ifEmpty { null })
                )
            }
        }

        override suspend fun generateAiDraft(
            simpleDesc: String?,
            content: String
        ): Result<Pair<String, String>> =
            safeApiCall(json) {
                recruitApiService.generateRecruitAiDraft(AiDraftRequest(simpleDesc = simpleDesc, content = content))
            }.map { it.data.simpleDesc to it.data.content }

        override suspend fun generateApplyAiDraft(
            postId: String,
            message: String
        ): Result<String> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCall(json) {
                recruitApiService.generateApplyAiDraft(id, ApplyAiDraftRequest(message = message))
            }.map { it.data.convertedText }
        }

        override suspend fun createProject(
            postId: String,
            name: String
        ): Result<ProjectCreationResult> {
            val id = postId.toValidRecruitId() ?: return Result.failure(invalidPostIdException(postId))
            return safeApiCall(json) {
                recruitApiService.createProject(id, ProjectCreateRequest(name = name))
            }.map { envelope ->
                ProjectCreationResult(
                    projectId = envelope.data.projectId.toString(),
                    chatRoomId = envelope.data.chatRoomId.toString()
                )
            }
        }

        private fun buildWriteRequest(
            title: String,
            maxParticipants: Int,
            onCampus: CampusScope,
            categoryIds: List<Int>,
            topicIds: List<Int>,
            startDate: LocalDate,
            endDate: LocalDate,
            shortIntro: String,
            detailContent: String
        ) = RecruitWriteRequest(
            title = title,
            maxMembers = maxParticipants,
            onCampus = onCampus == CampusScope.INTERNAL,
            categoryIds = categoryIds,
            topicIds = topicIds,
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            simpleDesc = shortIntro,
            content = detailContent
        )

        private fun RecruitSummaryDto.toDomain(): RecruitPostSummary =
            RecruitPostSummary(
                id = recruitId.toString(),
                title = title,
                authorId = authorId?.toString().orEmpty(),
                authorNickname = authorNickname,
                authorExperience = authorEXP,
                maxParticipants = maxMembers,
                availableSlots = availableSlots,
                status = status.toRecruitStatus(),
                createdAt = parseIsoOffsetDateTime(createdAt)
            )

        private fun RecruitDetailDto.toDomain(
            allCategories: List<RecruitCategory>,
            allTopics: List<RecruitTopic>
        ): RecruitPost =
            RecruitPost(
                id = recruitId.toString(),
                title = title,
                authorId = authorId?.toString().orEmpty(),
                authorNickname = authorNickname,
                authorExperience = authorEXP,
                onCampus = if (onCampus) CampusScope.INTERNAL else CampusScope.EXTERNAL,
                categories = category.mapNotNull { id -> allCategories.find { it.id == id } },
                topics = topics.mapNotNull { id -> allTopics.find { it.id == id } },
                startDate = LocalDate.parse(startDate),
                endDate = LocalDate.parse(endDate),
                maxParticipants = maxMembers,
                currentParticipants = maxMembers - availableSlots,
                shortIntro = simpleDesc,
                detailContent = content,
                status = status.toRecruitStatus(),
                createdAt = parseIsoOffsetDateTime(createdAt),
                isScrapped = isScrapped
            )

        /** 서버가 주는 댓글 트리(`replies` 중첩)를 [parentCommentId]로 연결된 평탄한 목록으로 바꾼다. */
        private fun CommentDto.flatten(
            postId: String,
            parentCommentId: String? = null
        ): List<RecruitComment> {
            val current =
                RecruitComment(
                    id = commentId.toString(),
                    postId = postId,
                    parentCommentId = parentCommentId,
                    authorId = authorId?.toString().orEmpty(),
                    authorNickname = authorNickname.orEmpty(),
                    content = content,
                    createdAt = parseIsoOffsetDateTime(createdAt),
                    isDeleted = authorNickname == null,
                    isAuthor = isAuthor
                )
            return listOf(current) + replies.flatMap { it.flatten(postId, commentId.toString()) }
        }
    }

private fun String.toValidRecruitId(): Long? = toLongOrNull()

private fun invalidPostIdException(id: String) = IllegalArgumentException("잘못된 id: $id")

private fun String.toRecruitStatus(): RecruitStatus =
    runCatching { RecruitStatus.valueOf(this) }.getOrDefault(RecruitStatus.OPEN)
