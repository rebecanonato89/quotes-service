package com.seguro.quotes.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.genai.Client
import com.seguro.quotes.ai.dto.AiRiskAssessment
import com.seguro.quotes.ai.dto.AiRiskInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GeminiAiRiskClientSdk(
    private val geminiClient: Client
) : AiRiskClient {
    private val logger = LoggerFactory.getLogger(GeminiAiRiskClientSdk::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override suspend fun assess(input: AiRiskInput): AiRiskAssessment {
        val callId = UUID.randomUUID().toString()
        val quoteId = input.document.takeIf { it.isNotBlank() } ?: "unknown"
        val prompt = buildPrompt(input)
        logger.info("[Gemini][callId={}] [quoteId={}] Prompt: {}", callId, quoteId, prompt)
        return try {
            val response = geminiClient.models.generateContent(
                "gemini-2.5-flash",
                prompt,
                null
            )
            val text = response.text() ?: ""
            logger.info("[Gemini][callId={}] [quoteId={}] Model response: {}", callId, quoteId, text)
            val assessment = parseAndSanitize(text)
            logger.info("[Gemini][callId={}] [quoteId={}] Parsed assessment: {}", callId, quoteId, assessment)
            assessment
        } catch (ex: Exception) {
            logger.error("[Gemini][callId={}] [quoteId={}] Error: {}", callId, quoteId, ex.message)
            AiRiskAssessment(
                riskScore = 50,
                reasons = listOf("AI_UNAVAILABLE"),
                summary = "AI_UNAVAILABLE"
            )
        }
    }

    private fun buildPrompt(input: AiRiskInput): String =
        """
        You are an insurance risk assessment AI. Reply ONLY with a valid compact JSON object, no explanation, no markdown, no code block. The JSON must match this schema:
        { "riskScore": 0-100, "reasons": [string], "summary": string }
        Data:
        document: ${input.document}
        insuranceType: ${input.insuranceType}
        age: ${input.age}
        make: ${input.make}
        model: ${input.model}
        zipCode: ${input.zipCode}
        coverages: ${input.coverages.joinToString(",")}
        """.trimIndent()

    private fun parseAndSanitize(text: String): AiRiskAssessment {
        return try {
            val parsed: AiRiskAssessment = objectMapper.readValue(text)
            AiRiskAssessment(
                riskScore = parsed.riskScore.coerceIn(0, 100),
                reasons = parsed.reasons.take(10),
                summary = parsed.summary.take(240)
            )
        } catch (e: Exception) {
            AiRiskAssessment(50, listOf("AI_UNAVAILABLE"), "AI_UNAVAILABLE")
        }
    }
}
