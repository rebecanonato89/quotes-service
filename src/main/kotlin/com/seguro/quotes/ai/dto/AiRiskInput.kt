package com.seguro.quotes.ai.dto

import com.seguro.quotes.dto.QuoteRequest

data class AiRiskInput(
    val document: String,
    val insuranceType: String,
    val age: Int?,
    val make: String?,
    val model: String?,
    val zipCode: String?,
    val coverages: List<String>
) {
    companion object {
        fun from(request: QuoteRequest): AiRiskInput {
            return AiRiskInput(
                document = request.document,
                insuranceType = request.insuranceType.name,
                age = request.age,
                make = request.vehicle?.make,
                model = request.vehicle?.model,
                zipCode = request.zipCode,
                coverages = request.coverages.map { it.name }
            )
        }
    }

    fun toPromptString(): String =
        "Document: $document, Insurance type: $insuranceType, Age: $age, Make: $make, Model: $model, Zip code: $zipCode, Coverages: ${coverages.joinToString(",")}"
}
