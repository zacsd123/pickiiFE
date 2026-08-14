package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.ProjectApiService
import com.example.pickii.domain.repository.ProjectRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** `6. Project` API로 [ProjectRepository]를 구현한다. */
@Singleton
class ProjectApiRepository
    @Inject
    constructor(
        private val apiService: ProjectApiService,
        private val json: Json
    ) : ProjectRepository {
        override suspend fun getProjectLeaderId(projectId: Long): Result<Long> =
            safeApiCall(json) { apiService.getProject(projectId) }.map { it.data.leaderId }

        override suspend fun closeProject(projectId: Long): Result<Unit> =
            safeApiCallUnit(json) { apiService.closeProject(projectId) }
    }
