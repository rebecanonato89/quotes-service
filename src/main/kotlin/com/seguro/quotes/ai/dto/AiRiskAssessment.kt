package com.seguro.quotes.ai.dto

data class AiRiskAssessment(
    val riskScore: Int,
    val reasons: List<String>,
    val summary: String
) {
    companion object {
        fun fallback(
            summary: String = "AI_UNAVAILABLE",
            reasons: List<String> = listOf("AI_UNAVAILABLE"),
            riskScore: Int = 50
        ): AiRiskAssessment = AiRiskAssessment(
            riskScore = riskScore.coerceIn(0, 100),
            reasons = reasons.take(10),
            summary = summary.take(240)
        )
    }

    fun normalized(): AiRiskAssessment = copy(
        riskScore = riskScore.coerceIn(0, 100),
        reasons = reasons.take(10),
        summary = summary.take(240)
    )
}
