package com.example.pickii.domain.repository

import com.example.pickii.domain.model.AiFeedback
import com.example.pickii.domain.model.FeedbackProjectMembers
import com.example.pickii.domain.model.FeedbackProjectPage
import com.example.pickii.domain.model.FeedbackScores

/** 상호평가/피드백(4-9~4-12)을 담당한다. */
interface FeedbackRepository {
    suspend fun getMyFeedbackProjects(
        page: Int = 0,
        size: Int = 10
    ): Result<FeedbackProjectPage>

    suspend fun getProjectMembers(projectId: String): Result<FeedbackProjectMembers>

    suspend fun submitFeedback(
        projectId: String,
        revieweeId: String,
        scores: FeedbackScores,
        strength: String,
        weakness: String
    ): Result<Unit>

    suspend fun getAiFeedback(projectId: String): Result<AiFeedback>
}
