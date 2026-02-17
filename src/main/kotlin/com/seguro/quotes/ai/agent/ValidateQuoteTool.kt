package com.seguro.quotes.ai.agent

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seguro.quotes.common.Either
import com.seguro.quotes.domain.validation.QuoteValidator
import com.seguro.quotes.dto.QuoteRequest

class ValidateQuoteTool : Tool {

    override val name = "validateQuote"
    override val description = "Validates a quote request."
    override val inputSchema: String = """
    {
      "type": "object",
      "description": "QuoteRequest JSON"
    }
    """.trimIndent()

    private val objectMapper = jacksonObjectMapper()

    override suspend fun execute(argsJson: String): String {

        val request = try {
            objectMapper.readValue<QuoteRequest>(argsJson)
        } catch (e: Exception) {
            return objectMapper.writeValueAsString(
                mapOf(
                    "valid" to false,
                    "errors" to listOf("Invalid QuoteRequest JSON: ${e.message}")
                )
            )
        }

        val validation = QuoteValidator.validate(request)

        return when (validation) {
            is Either.Right -> {
                objectMapper.writeValueAsString(
                    mapOf("valid" to true, "errors" to emptyList<String>())
                )
            }
            is Either.Left -> {
                objectMapper.writeValueAsString(
                    mapOf(
                        "valid" to false,
                        "errors" to listOf(validation.value.message)
                    )
                )
            }
        }
    }
}
