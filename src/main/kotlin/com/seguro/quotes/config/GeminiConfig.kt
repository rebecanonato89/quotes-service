package com.seguro.quotes.config

import com.google.genai.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeminiConfig {

    @Bean
    fun geminiClient(): Client {
        // O SDK lê a chave do ambiente (GOOGLE_API_KEY).
        // Se não existir, ele vai falhar ao chamar o modelo.
        return Client.builder().build()
    }
}
