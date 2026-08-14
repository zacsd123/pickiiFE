package com.example.pickii.util

/** 레벨 구간 임계값(경험치). 이 값을 초과해야 다음 레벨이다 — 0~20:1레벨, 21~60:2레벨, 61~120:3레벨, 121 이상:4레벨(상한 없음). */
private val LEVEL_THRESHOLDS = listOf(20, 60, 120)

/**
 * 경험치([exp])로부터 레벨/진행률을 계산한 결과.
 *
 * @param level 현재 레벨(1부터 시작)
 * @param progress 경험치 바에 표시할 0f~1f 사이 진행률
 */
data class LevelInfo(
    val level: Int,
    val progress: Float
)

/** [exp]로 레벨/경험치 바 진행률을 계산한다. */
fun calculateLevel(exp: Int): LevelInfo {
    val safeExp = exp.coerceAtLeast(0)
    val level = 1 + LEVEL_THRESHOLDS.count { safeExp > it }
    val lowerBound = LEVEL_THRESHOLDS.lastOrNull { safeExp > it } ?: 0
    val upperBound = LEVEL_THRESHOLDS.firstOrNull { safeExp <= it }
    val progress =
        if (upperBound == null) {
            1f
        } else {
            (safeExp - lowerBound).toFloat() / (upperBound - lowerBound)
        }
    return LevelInfo(level = level, progress = progress)
}
