package com.seguro.quotes.dto

data class PriceResult(
    val price: Double,
    val approved: Boolean,
    val rejectionReason: String?
)