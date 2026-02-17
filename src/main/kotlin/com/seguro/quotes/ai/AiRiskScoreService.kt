package com.seguro.quotes.ai

import com.seguro.quotes.ai.dto.AiRiskAssessment
import com.seguro.quotes.ai.dto.AiRiskInput
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AiRiskScoreService(
    private val aiRiskClient: AiRiskClient
) {
    private val logger = LoggerFactory.getLogger(AiRiskScoreService::class.java)

    companion object {
        // POC: 1 chamada concorrente para evitar estourar quota
        private val semaphore = Semaphore(1)
    }

    suspend fun calculateRiskScore(input: AiRiskInput, origin: String? = null): AiRiskAssessment {
        return semaphore.withPermit {
            try {
                val result = aiRiskClient.assess(input).normalized()
                logger.info("[AiRiskScoreService] origin={} riskScore={} reasonsCount={}",
                    origin ?: "unknown",
                    result.riskScore,
                    result.reasons.size
                )
                result
            } catch (e: Exception) {
                logger.warn("[AiRiskScoreService] origin={} AI failed: {}. Using fallback.",
                    origin ?: "unknown",
                    e.message
                )
                AiRiskAssessment.fallback()
            }
        }
    }
}
