package com.seguro.quotes.ai.agent

/**
 * Tool abstraction for agent tool calling (function calling).
 */
interface Tool {
    val name: String
    val description: String
    val inputSchema: String // JSON Schema as String
    suspend fun execute(argsJson: String): String // Returns JSON result (JSON string)
}

