package com.seguro.quotes.domain.enums


enum class InsuranceType(val basePrice: Double) {
    AUTO(100.0),
    HOME(150.0),
    LIFE(200.0)
}