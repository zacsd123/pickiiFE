package com.example.pickii.data.repository

import com.example.pickii.domain.model.CampusScope
import com.example.pickii.domain.model.RecruitApplication
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitComment
import com.example.pickii.domain.model.RecruitPost
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.repository.RecruitRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** 목업 Repository의 읽기/쓰기 동작이 서버 호출처럼 느껴지도록 흉내 내는 지연 시간(ms). */
private const val MOCK_OPERATION_DELAY_MS = 400L

/** AI 초안 생성이 서버 호출처럼 느껴지도록 흉내 내는 지연 시간(ms). */
private const val AI_GENERATION_DELAY_MS = 1200L

/** AI 초안 생성 실패를 재현 가능하게 만들기 위한 호출 간격. 무작위 실패는 데모 중 재현이 어려워 매 N번째 호출마다 결정적으로 실패시킨다. */
private const val AI_FAILURE_INTERVAL = 3

/** 시드로 채워 넣는 목업 모집 글 개수. */
private const val MOCK_POST_COUNT = 30

/** 목업 모집 글의 최소 모집 인원. */
private const val MIN_MOCK_PARTICIPANTS = 2

/** 목업 모집 글의 모집 인원이 오갈 수 있는 범위 폭. */
private const val MOCK_PARTICIPANTS_RANGE = 6

/** 몇 개 글마다 하나를 마감(CLOSED) 상태로 시드할지. 0번째 글은 데모용으로 항상 모집 중을 유지한다. */
private const val MOCK_CLOSED_INTERVAL = 7

/** 목업 작성자 닉네임이 순환되는 개수. */
private val MockAuthorNicknames = listOf("김기획", "박디자인", "이학술", "최아이티", "정프로젝트")

/** 목업 작성자의 기본 경험치. */
private const val MOCK_BASE_EXPERIENCE = 40

/** 목업 모집 글의 시작일이 오늘로부터 며칠 뒤인지, 글 순서(index)에 곱해지는 간격(일). */
private const val MOCK_START_OFFSET_DAYS = 3L

/** 목업 모집 글의 진행 기간(일). */
private const val MOCK_DURATION_DAYS = 30L

/**
 * 백엔드 없이 모집 글/댓글/스크랩/지원/AI 초안 생성을 흉내 내는 목업 Repository.
 *
 * 모든 상태는 메모리에만 보관되며 앱 재시작 시 초기화된다. `posts`는 전체 화면이 공유하는
 * 단일 진실 공급원이라, 등록/수정/삭제한 결과가 홈/상세 화면에 별도 재조회 없이 바로 반영된다.
 */
@Singleton
class MockRecruitRepository
    @Inject
    constructor() : RecruitRepository {
        private val posts = MutableStateFlow(seedMockPosts())
        private val commentsByPost = mutableMapOf<String, MutableStateFlow<List<RecruitComment>>>()
        private val scrappedKeys = mutableSetOf<Pair<String, String>>()
        private val applications = mutableListOf<RecruitApplication>()

        /** AI 초안 생성 호출 횟수. [AI_FAILURE_INTERVAL]번째 호출마다 실패를 재현하기 위한 목업 전용 상태다. */
        private var aiCallCount = 0

        override fun observePosts(): StateFlow<List<RecruitPost>> = posts.asStateFlow()

        override fun getPostById(postId: String): RecruitPost? = posts.value.firstOrNull { it.id == postId }

        override suspend fun createPost(post: RecruitPost): Result<RecruitPost> {
            delay(MOCK_OPERATION_DELAY_MS)
            posts.update { it + post }
            return Result.success(post)
        }

        override suspend fun updatePost(post: RecruitPost): Result<RecruitPost> {
            delay(MOCK_OPERATION_DELAY_MS)
            posts.update { list -> list.map { if (it.id == post.id) post else it } }
            return Result.success(post)
        }

        override suspend fun closePost(postId: String): Result<Unit> {
            delay(MOCK_OPERATION_DELAY_MS)
            posts.update { list -> list.map { if (it.id == postId) it.copy(status = RecruitStatus.CLOSED) else it } }
            return Result.success(Unit)
        }

        override suspend fun deletePost(postId: String): Result<Unit> {
            delay(MOCK_OPERATION_DELAY_MS)
            posts.update { list -> list.filterNot { it.id == postId } }
            return Result.success(Unit)
        }

        override fun observeComments(postId: String): StateFlow<List<RecruitComment>> =
            commentsFlowFor(postId).asStateFlow()

        override suspend fun addComment(
            postId: String,
            authorId: String,
            authorNickname: String,
            parentCommentId: String?,
            content: String
        ): Result<RecruitComment> {
            delay(MOCK_OPERATION_DELAY_MS)
            val comment =
                RecruitComment(
                    id = UUID.randomUUID().toString(),
                    postId = postId,
                    parentCommentId = parentCommentId,
                    authorId = authorId,
                    authorNickname = authorNickname,
                    content = content,
                    createdAt = LocalDateTime.now()
                )
            commentsFlowFor(postId).update { it + comment }
            return Result.success(comment)
        }

        override suspend fun deleteComment(commentId: String): Result<Unit> {
            delay(MOCK_OPERATION_DELAY_MS)
            val flow =
                commentsByPost.values.firstOrNull { flow -> flow.value.any { it.id == commentId } }
                    ?: return Result.failure(NoSuchElementException("댓글을 찾을 수 없습니다: $commentId"))
            flow.update { list -> list.map { if (it.id == commentId) it.copy(isDeleted = true) else it } }
            return Result.success(Unit)
        }

        override fun isScraped(
            postId: String,
            userId: String
        ): Boolean = (postId to userId) in scrappedKeys

        override suspend fun toggleScrap(
            postId: String,
            userId: String
        ): Result<Boolean> {
            delay(MOCK_OPERATION_DELAY_MS)
            val key = postId to userId
            val isScrapedNow =
                if (key in scrappedKeys) {
                    scrappedKeys.remove(key)
                    false
                } else {
                    scrappedKeys.add(key)
                    true
                }
            return Result.success(isScrapedNow)
        }

        override fun hasApplied(
            postId: String,
            userId: String
        ): Boolean = applications.any { it.postId == postId && it.applicantId == userId }

        override suspend fun submitApplication(
            postId: String,
            applicantId: String,
            applicantNickname: String,
            message: String
        ): Result<RecruitApplication> {
            delay(MOCK_OPERATION_DELAY_MS)
            val application =
                RecruitApplication(
                    id = UUID.randomUUID().toString(),
                    postId = postId,
                    applicantId = applicantId,
                    applicantNickname = applicantNickname,
                    message = message,
                    appliedAt = LocalDateTime.now()
                )
            applications.add(application)
            posts.update { list ->
                list.map { if (it.id == postId) it.copy(currentParticipants = it.currentParticipants + 1) else it }
            }
            return Result.success(application)
        }

        override suspend fun generateAiDraft(prompt: String): Result<String> {
            delay(AI_GENERATION_DELAY_MS)
            aiCallCount += 1
            return if (aiCallCount % AI_FAILURE_INTERVAL == 0) {
                Result.failure(IllegalStateException("AI 초안 생성에 실패했습니다."))
            } else {
                Result.success(buildMockAiDraft(prompt))
            }
        }

        /** [postId]의 댓글 [StateFlow]를 가져오거나, 없으면 시드 데이터로 새로 만든다. */
        private fun commentsFlowFor(postId: String): MutableStateFlow<List<RecruitComment>> =
            commentsByPost.getOrPut(postId) { MutableStateFlow(seedMockComments(postId)) }
    }

/** 입력된 프롬프트를 반영한 것처럼 보이는 목업 AI 초안 문구를 만든다. */
private fun buildMockAiDraft(prompt: String): String = "$prompt 함께할 팀원을 찾고 있어요! 열정 있는 분들의 많은 지원 부탁드립니다."

/** 홈/상세/지원 화면을 데모할 수 있도록 카테고리·주제·상태·기간을 골고루 갖춘 목업 모집 글을 만든다. */
private fun seedMockPosts(): List<RecruitPost> {
    val categories = RecruitCategory.entries
    val topics = RecruitTopic.entries.filter { it.isEnabled }
    val campusScopes = CampusScope.entries
    val today = LocalDate.now()

    return List(MOCK_POST_COUNT) { index ->
        val category = categories[index % categories.size]
        val maxParticipants = MIN_MOCK_PARTICIPANTS + index % MOCK_PARTICIPANTS_RANGE
        val startDate = today.plusDays(index * MOCK_START_OFFSET_DAYS)
        val isDemoAuthorPost = index == 0

        RecruitPost(
            id = "post-$index",
            title = "${category.label} 팀원 모집 $index",
            authorId =
                if (isDemoAuthorPost) {
                    MockUserIds.CURRENT_USER_ID
                } else {
                    "author-${index % MockAuthorNicknames.size}"
                },
            authorNickname = MockAuthorNicknames[index % MockAuthorNicknames.size],
            authorExperience = MOCK_BASE_EXPERIENCE + index,
            onCampus = campusScopes[index % campusScopes.size],
            category = category,
            topic = topics[index % topics.size],
            startDate = startDate,
            endDate = startDate.plusDays(MOCK_DURATION_DAYS),
            maxParticipants = maxParticipants,
            currentParticipants = index % maxParticipants,
            shortIntro = "모집하는 [${category.label}]에 대해 간단히 소개해요!",
            detailContent =
                "안녕하세요! 저희 팀은 ${category.label}을(를) 함께할 팀원을 모집하고 있어요. " +
                    "적극적으로 참여하실 분들의 많은 지원 부탁드립니다!",
            status =
                if (!isDemoAuthorPost &&
                    index % MOCK_CLOSED_INTERVAL == 0
                ) {
                    RecruitStatus.CLOSED
                } else {
                    RecruitStatus.OPEN
                },
            createdAt = LocalDateTime.now().minusDays(index.toLong())
        )
    }
}

/** 댓글/답글 트리(들여쓰기, 무제한 깊이) 렌더링을 데모할 수 있도록 첫 번째 목업 글에만 댓글 스레드를 시드한다. */
private fun seedMockComments(postId: String): List<RecruitComment> {
    if (postId != "post-0") return emptyList()

    val now = LocalDateTime.now()
    val rootComment =
        RecruitComment(
            id = "seed-comment-1",
            postId = postId,
            parentCommentId = null,
            authorId = "author-1",
            authorNickname = "박디자인",
            content = "혹시 기획 경험이 없어도 지원 가능한가요?",
            createdAt = now.minusDays(2)
        )
    val reply =
        RecruitComment(
            id = "seed-comment-2",
            postId = postId,
            parentCommentId = rootComment.id,
            authorId = MockUserIds.CURRENT_USER_ID,
            authorNickname = "김기획",
            content = "네, 열정 있으시면 환영합니다!",
            createdAt = now.minusDays(1)
        )
    val replyToReply =
        RecruitComment(
            id = "seed-comment-3",
            postId = postId,
            parentCommentId = reply.id,
            authorId = "author-1",
            authorNickname = "박디자인",
            content = "감사합니다! 바로 지원해볼게요.",
            createdAt = now.minusHours(12)
        )
    return listOf(rootComment, reply, replyToReply)
}
