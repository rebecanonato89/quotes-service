package com.seguro.quotes.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun coroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.Default)
}

