package com.example.pickii.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * java.time -> kotlinx-datetime 전환용 "현재 시각" 진입점을 한 곳으로 모은다. 기존 코드 곳곳의
 * `LocalDate.now()`/`LocalDateTime.now()`(시스템 기본 타임존 기준)와 동일하게 [TimeZone.currentSystemDefault]를
 * 쓴다 — 타임존을 Asia/Seoul 등으로 고정하는 건 동작 변경이라 이번 전환 스코프가 아니다.
 */
@OptIn(ExperimentalTime::class)
fun nowDateTime(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun today(): LocalDate = nowDateTime().date
