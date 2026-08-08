package com.example.pickii.util

// TODO: 레벨 산출 공식 기획/백엔드 확정 필요 — 현재는 exp 100당 1레벨로 클라이언트에서 임의 계산한다.
private const val EXP_PER_LEVEL = 100

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
    val level = 1 + safeExp / EXP_PER_LEVEL
    val expInLevel = safeExp % EXP_PER_LEVEL
    return LevelInfo(
        level = level,
        progress = expInLevel / EXP_PER_LEVEL.toFloat()
    )
}
