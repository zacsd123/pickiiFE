package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.ProfileApiService
import com.example.pickii.data.remote.dto.AdditionalLinkDto
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.CreateResumeRequest
import com.example.pickii.data.remote.dto.CreateResumeResponseDto
import com.example.pickii.data.remote.dto.ExperienceDto
import com.example.pickii.data.remote.dto.LicenseDto
import com.example.pickii.data.remote.dto.MemberProfileDto
import com.example.pickii.data.remote.dto.SkillToolDto
import com.example.pickii.data.remote.dto.UpdateResumeRequest
import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.AdditionalLinkEntry
import com.example.pickii.domain.model.CreateProfileInput
import com.example.pickii.domain.model.ExperienceEntry
import com.example.pickii.domain.model.LicenseEntry
import com.example.pickii.domain.model.MemberProfile
import com.example.pickii.domain.model.SkillToolEntry
import com.example.pickii.domain.model.UpdateProfileInput
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.datetime.YearMonth

private const val ERROR_CODE_RESUME_NOT_FOUND = "RESUME_NOT_FOUND"

/** `4-1 내 프로필 조회`, `4-2 프로필 생성`으로 [ProfileRepository]를 구현한다. */
class ProfileApiRepository
    constructor(
        private val profileApiService: ProfileApiService
    ) : ProfileRepository {
        override suspend fun getMyProfile(): Result<MemberProfile> =
            safeApiCall<ApiEnvelope<MemberProfileDto>> { profileApiService.getMyProfile() }.map { it.data.toDomain() }

        override suspend fun hasResume(): Boolean =
            getMyProfile().fold(
                onSuccess = { true },
                onFailure = { error -> !(error is ApiException && error.code == ERROR_CODE_RESUME_NOT_FOUND) }
            )

        override suspend fun createProfile(input: CreateProfileInput): Result<Unit> =
            safeApiCall<ApiEnvelope<CreateResumeResponseDto>> {
                profileApiService.createResume(input.toRequest())
            }.map { }

        override suspend fun updateProfile(input: UpdateProfileInput): Result<Unit> =
            safeApiCallUnit { profileApiService.updateResume(input.toRequest()) }

        override suspend fun getMemberProfile(memberId: String): Result<MemberProfile> {
            val id = memberId.toLongOrNull() ?: return Result.failure(IllegalArgumentException("잘못된 id: $memberId"))
            return safeApiCall<ApiEnvelope<MemberProfileDto>> {
                profileApiService.getMemberProfile(id)
            }.map { it.data.toDomain() }
        }

        private fun UpdateProfileInput.toRequest() =
            UpdateResumeRequest(
                univId = univId.toLong(),
                major = major,
                academicStatus = academicStatus.name,
                hope = hope?.ifBlank { null },
                strength = strength?.ifBlank { null },
                aboutMe = aboutMe?.ifBlank { null },
                contactEmail = contactEmail,
                topic = topicIds,
                skillTool = skillTools.map { SkillToolDto(techStackName = it.techStackName, level = it.level) },
                license = licenses.map { LicenseDto(licenseName = it.licenseName, date = it.acquiredDate.toString()) },
                experience =
                    experiences.map {
                        ExperienceDto(
                            startDate = it.startDate.toString(),
                            endDate = it.endDate?.toString(),
                            title = it.title,
                            organization = it.organization,
                            description = it.description
                        )
                    },
                additionalLink = additionalLinks.map { AdditionalLinkDto(linkName = it.linkName, url = it.url) }
            )

        private fun CreateProfileInput.toRequest() =
            CreateResumeRequest(
                univId = univId.toLong(),
                major = major,
                academicStatus = academicStatus.name,
                hope = hope?.ifBlank { null },
                strength = strength?.ifBlank { null },
                topic = topicIds,
                skillTool = skillTools.map { SkillToolDto(techStackName = it.techStackName, level = it.level) },
                license = licenses.map { LicenseDto(licenseName = it.licenseName, date = it.acquiredDate.toString()) },
                experience =
                    experiences.map {
                        ExperienceDto(
                            startDate = it.startDate.toString(),
                            endDate = it.endDate?.toString(),
                            title = it.title,
                            organization = it.organization,
                            description = it.description
                        )
                    },
                additionalLink = additionalLinks.map { AdditionalLinkDto(linkName = it.linkName, url = it.url) }
            )

        private fun MemberProfileDto.toDomain(): MemberProfile =
            MemberProfile(
                nickname = nickname,
                univId = univId.toInt(),
                univ = univ,
                major = major,
                academicStatus =
                    runCatching { AcademicStatus.valueOf(academicStatus) }.getOrDefault(AcademicStatus.ENROLLED),
                hope = hope,
                strength = strength,
                aboutMe = aboutMe,
                contactEmail = contactEmail,
                exp = exp,
                topicIds = topic,
                skillTools = skillTool.map { SkillToolEntry(techStackName = it.techStackName, level = it.level) },
                licenses =
                    license.map { LicenseEntry(licenseName = it.licenseName, acquiredDate = YearMonth.parse(it.date)) },
                experiences =
                    experience.map {
                        ExperienceEntry(
                            startDate = YearMonth.parse(it.startDate),
                            endDate = it.endDate?.let(YearMonth::parse),
                            title = it.title,
                            organization = it.organization,
                            description = it.description
                        )
                    },
                additionalLinks = additionalLink.map { AdditionalLinkEntry(linkName = it.linkName, url = it.url) }
            )
    }
