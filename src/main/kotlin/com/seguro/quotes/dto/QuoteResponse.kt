package com.seguro.quotes.dto

import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.domain.model.Quote
import java.time.LocalDateTime
import java.util.UUID

data class QuoteResponse(
    val id: UUID,
    val status: QuoteStatus,
    val price: Double?,
    val rejectionReasons: List<String>,
    val timestamp: LocalDateTime
) {
    companion object {
        // Factory method: convert Quote → QuoteResponse
        fun from(quote: Quote): QuoteResponse = QuoteResponse(
            id = quote.id,
            status = quote.status,
            price = quote.price,
            rejectionReasons = quote.rejectionReasons,
            timestamp = quote.timestamp
        )
    }
}