package com.seguro.quotes.domain.extensions

import com.seguro.quotes.domain.event.DomainEvent
import com.seguro.quotes.domain.event.QuoteApprovedEvent
import com.seguro.quotes.domain.event.QuoteRejectedEvent
import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.common.maskDocument

// Total de coberturas
fun Quote.totalCoverages(): Int = this.request.coverages.size

// Documento mascarado (para logs)
fun Quote.maskedDocument(): String = this.request.document.maskDocument()

// Log seguro (sem expor dados sensíveis)
fun Quote.toSafeLogString(): String = buildString {
    append("Quote[")
    append("id=${id}, ")
    append("status=${status}, ")
    append("preco=${price}, ")
    append("documento=${maskedDocument()}, ")  // mascarado!
    append("coberturas=${totalCoverages()}")
    append("]")
}

// Alternativa usando apply:
fun Quote.logSafe(): Quote = apply {
    println(toSafeLogString())
}

fun Quote.toApprovedEvent(): DomainEvent? =
    if (status == com.seguro.quotes.domain.enums.QuoteStatus.APPROVED && price != null)
        QuoteApprovedEvent(
            quoteId = id,
            price = price,
            insuranceType = request.insuranceType.name
        )
    else null

fun Quote.toRejectedEvent(): DomainEvent? =
    if (status == com.seguro.quotes.domain.enums.QuoteStatus.REJECTED)
        QuoteRejectedEvent(
            quoteId = id,
            reasons = rejectionReasons
        )
    else null
