package com.seguro.quotes.domain.service

import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.domain.enums.Coverage
import com.seguro.quotes.domain.enums.InsuranceType
import com.seguro.quotes.dto.PriceResult

// Type alias para estratégia de pricing
typealias PricingStrategy = (QuoteRequest) -> Double

object PriceCalculator {

    private const val LIMIT_MAX = 300.0

    // Calcular preço completo
    fun calculate(request: QuoteRequest): PriceResult {
        val basePrice = request.insuranceType.basePrice
        val coveragesCost = calculateCoveragesCost(request.coverages)
        val ageFactor = calculateAgeFactor(request)

        val totalPrice = (basePrice + coveragesCost) * ageFactor

        val isApproved = totalPrice <= LIMIT_MAX
        val rejectionReason = if (isApproved) null else "LIMIT_EXCEEDED"

        return PriceResult(
            price = totalPrice,
            approved = isApproved,
            rejectionReason = rejectionReason
        )
    }

    // Soma custos de coberturas (usando fold)
    private fun calculateCoveragesCost(coverages: List<Coverage>): Double {
        return coverages.fold(0.0) { acc, coverage ->
            acc + coverage.additionalCost
        }
        // Alternativa com sumOf:
        // return coverages.sumOf { it.additionalCost }
    }

    // Fator de idade (VIDA)
    private fun calculateAgeFactor(request: QuoteRequest): Double {
        // Se não é VIDA, fator = 1.0 (sem alteração)
        if (request.insuranceType != InsuranceType.LIFE) return 1.0

        val idade = request.age ?: return 1.0

        return when {
            idade < 25 -> 1.20  // +20%
            idade <= 50 -> 1.0  // +0%
            else -> 1.30        // +30%
        }
    }
}