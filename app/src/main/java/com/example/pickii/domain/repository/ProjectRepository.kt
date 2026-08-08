package com.example.pickii.domain.repository

/** `6. Project` API로 프로젝트 정보를 다룬다. */
interface ProjectRepository {
    /** `GET /projects/{projectId}`(6-2)의 `leaderId`만 필요해서 그 값만 뽑아 반환한다. */
    suspend fun getProjectLeaderId(projectId: Long): Result<Long>

    /** `PATCH /projects/{projectId}/close`(6-4). 프로젝트장만 호출 가능하다. */
    suspend fun closeProject(projectId: Long): Result<Unit>
}
