package com.seguro.quotes.ai.agent

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.seguro.quotes.service.RiskScoreService

/**
 * Tool to simulate risk score using RiskScoreService.
 * Input: { "document": string }
 * Output: { "riskScore": int }
 */
class SimulatedRiskScoreTool(
    private val riskScoreService: RiskScoreService
) : Tool {

    override val name = "simulatedRiskScore"
    override val description = "Simulates a risk score for a document."
    override val inputSchema: String = """
    {
      "$${"schema"}": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "properties": {
        "document": { "type": "string" }
      },
      "required": ["document"]
    }
    """.trimIndent()

    private val objectMapper = jacksonObjectMapper()

    override suspend fun execute(argsJson: String): String {
        val node = try {
            objectMapper.readTree(argsJson)
        } catch (e: Exception) {
            return objectMapper.writeValueAsString(
                mapOf("error" to "INVALID_INPUT", "message" to (e.message ?: "unknown"))
            )
        }

        val document = node.get("document")?.asText()
        if (document.isNullOrBlank()) {
            return objectMapper.writeValueAsString(
                mapOf("error" to "INVALID_INPUT", "message" to "Missing field: document")
            )
        }

        val score = riskScoreService.calculateRiskScore(document)
        return objectMapper.writeValueAsString(mapOf("riskScore" to score))
    }
}
