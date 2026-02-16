package com.seguro.quotes.service

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RiskScoreService {

    // Simula chamada externa (API de score de risco)
    suspend fun calculateRiskScore(documento: String): Int {
        // Simula latência de rede (500ms)
        delay(500)

        // Retorna score aleatório entre 0-100
        return Random.nextInt(0, 100)
    }
}