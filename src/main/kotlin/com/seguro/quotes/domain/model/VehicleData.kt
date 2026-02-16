package com.seguro.quotes.domain.model

import jakarta.validation.constraints.AssertTrue


data class VehicleData(
    val make: String?,
    val model: String?,
    val year: Int? = null
){
    @AssertTrue(message = "Vehicle must have at least make or model")
    fun isValid(): Boolean = !make.isNullOrBlank() || !model.isNullOrBlank() // Encapsulation - the class knows its rules
}