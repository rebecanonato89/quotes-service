package com.seguro.quotes.ai

import com.seguro.quotes.ai.dto.AiRiskAssessment
import com.seguro.quotes.ai.dto.AiRiskInput

interface AiRiskClient {
    suspend fun assess(input: AiRiskInput): AiRiskAssessment
}
