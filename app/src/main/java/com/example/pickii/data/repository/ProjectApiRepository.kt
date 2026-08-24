package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.ProjectApiService
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ProjectDetailDto
import com.example.pickii.domain.repository.ProjectRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit

/** `6. Project` API로 [ProjectRepository]를 구현한다. */
class ProjectApiRepository
    constructor(
        private val apiService: ProjectApiService
    ) : ProjectRepository {
        override suspend fun getProjectLeaderId(projectId: Long): Result<Long> =
            safeApiCall<ApiEnvelope<ProjectDetailDto>> { apiService.getProject(projectId) }.map { it.data.leaderId }

        override suspend fun closeProject(projectId: Long): Result<Unit> =
            safeApiCallUnit { apiService.closeProject(projectId) }
    }
