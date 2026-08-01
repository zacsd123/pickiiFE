package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.ProfileApiService
import com.example.pickii.data.remote.dto.AdditionalLinkDto
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.CreateResumeRequest
import com.example.pickii.data.remote.dto.ExperienceDto
import com.example.pickii.data.remote.dto.LicenseDto
import com.example.pickii.data.remote.dto.MemberProfileDto
import com.example.pickii.data.remote.dto.SkillToolDto
import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.AdditionalLinkEntry
import com.example.pickii.domain.model.CreateProfileInput
import com.example.pickii.domain.model.ExperienceEntry
import com.example.pickii.domain.model.LicenseEntry
import com.example.pickii.domain.model.MemberProfile
import com.example.pickii.domain.model.SkillToolEntry
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.util.network.safeApiCall
import kotlinx.serialization.json.Json
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

private const val ERROR_CODE_RESUME_NOT_FOUND = "RESUME_NOT_FOUND"

/** `4-1 내 프로필 조회`, `4-2 프로필 생성`으로 [ProfileRepository]를 구현한다. */
@Singleton
class ProfileApiRepository
    @Inject
    constructor(
        private val profileApiService: ProfileApiService,
        private val json: Json
    ) : ProfileRepository {
        override suspend fun getMyProfile(): Result<MemberProfile> =
            safeApiCall(json) { profileApiService.getMyProfile() }.map { it.data.toDomain() }

        override suspend fun hasResume(): Boolean =
            getMyProfile().fold(
                onSuccess = { true },
                onFailure = { error -> !(error is ApiException && error.code == ERROR_CODE_RESUME_NOT_FOUND) }
            )

        override suspend fun createProfile(input: CreateProfileInput): Result<Unit> =
            safeApiCall(json) { profileApiService.createResume(input.toRequest()) }.map { }

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
