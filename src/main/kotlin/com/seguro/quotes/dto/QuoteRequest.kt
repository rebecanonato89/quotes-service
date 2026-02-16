package com.seguro.quotes.dto

import com.seguro.quotes.domain.enums.Coverage
import com.seguro.quotes.domain.enums.InsuranceType
import com.seguro.quotes.domain.model.VehicleData
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class QuoteRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Document is required")
    val document: String, // CPF or CNPJ (free format)

    val email: String? = null, // Optional

    @field:NotNull(message = "Insurance type is required")
    val insuranceType: InsuranceType,

    // Required if LIFE
    @field:Min(value = 18, message = "Minimum age: 18 years")
    val age: Int? = null,

    // Required if AUTO
    @field:Valid // Validates VehicleData internally
    val vehicle: VehicleData? = null,

    val zipCode: String? = null,

    val coverages: List<Coverage> = emptyList() // Can be empty
)