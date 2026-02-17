package com.seguro.quotes.ai.agent

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seguro.quotes.domain.service.PriceCalculator
import com.seguro.quotes.dto.QuoteRequest

/**
 * Tool to calculate base price using PriceCalculator.
 * Input: QuoteRequest JSON
 * Output: { "basePrice": number }
 */
class CalculateBasePriceTool(
    private val priceCalculator: PriceCalculator = PriceCalculator
) : Tool {

    override val name = "calculateBasePrice"
    override val description = "Calculates the base price for a quote request."
    override val inputSchema: String = """
    {
      "$${"schema"}": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "description": "QuoteRequest JSON (must match backend QuoteRequest)."
    }
    """.trimIndent()

    private val objectMapper = jacksonObjectMapper()

    override suspend fun execute(argsJson: String): String {
        val request = try {
            objectMapper.readValue<QuoteRequest>(argsJson)
        } catch (e: Exception) {
            return objectMapper.writeValueAsString(
                mapOf(
                    "error" to "INVALID_INPUT",
                    "message" to ("Failed to parse QuoteRequest: " + (e.message ?: "unknown"))
                )
            )
        }

        val priceResult = try {
            PriceCalculator.calculate(request)
        } catch (e: Exception) {
            return objectMapper.writeValueAsString(
                mapOf(
                    "error" to "PRICE_CALCULATION_FAILED",
                    "message" to (e.message ?: "unknown")
                )
            )
        }

        return objectMapper.writeValueAsString(mapOf("basePrice" to priceResult.price))
    }
}
