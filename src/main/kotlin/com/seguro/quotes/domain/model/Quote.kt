package com.seguro.quotes.domain.model

import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.dto.QuoteRequest
import java.time.LocalDateTime
import java.util.UUID

data class Quote(
    val id: UUID = UUID.randomUUID(),
    val status: QuoteStatus,
    val price: Double? = null, // Null while not priced
    val rejectionReasons: List<String> = emptyList(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val request: QuoteRequest // Original request data
) {
    // Helper: check if policy can be generated
    fun canGeneratePolicy(): Boolean =
        status == QuoteStatus.APPROVED && price != null

    // Helper: check if expired (7 days without issuing policy)
    fun isExpired(): Boolean =
        timestamp.isBefore(LocalDateTime.now().minusDays(7))
}