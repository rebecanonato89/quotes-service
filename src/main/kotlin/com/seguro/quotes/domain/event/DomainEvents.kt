package com.seguro.quotes.domain.event

import com.seguro.quotes.domain.enums.InsuranceType
import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.domain.model.Policy
import java.time.LocalDateTime
import java.util.UUID

// Sealed class: todos os eventos possíveis
sealed class DomainEvent {
    abstract val eventId: UUID
    abstract val timestamp: LocalDateTime
}

// Evento: Quote aprovada
data class QuoteApprovedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val quoteId: UUID,
    val price: Double,
    val insuranceType: String
) : DomainEvent()

// Evento: Quote rejeitada
data class QuoteRejectedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val quoteId: UUID,
    val reasons : List<String>
) : DomainEvent()

// Evento: Policy emitida
data class PolicyIssuedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val policyId: UUID,
    val quoteId: UUID,
    val policyNumber: String
) : DomainEvent()

// Extension: criar evento a partir de Quote
fun Quote.toApprovedEvent(): QuoteApprovedEvent? {
    return if (canGeneratePolicy()) {
        QuoteApprovedEvent(
            quoteId = id,
            price = price!!,
            insuranceType = request.insuranceType.name
        )
    } else null
}

fun Quote.toRejectedEvent(): QuoteRejectedEvent? {
    return if (status == QuoteStatus.REJECTED) {
        QuoteRejectedEvent(
            quoteId = id,
            reasons = rejectionReasons
        )
    } else null
}

// Extension: criar evento a partir de Policy
fun Policy.toIssuedEvent(): PolicyIssuedEvent {
    return PolicyIssuedEvent(
        policyId = id,
        quoteId = quoteId,
        policyNumber = policyNumber
    )
}