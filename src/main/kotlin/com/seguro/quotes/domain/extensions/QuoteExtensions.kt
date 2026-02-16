package com.seguro.quotes.domain.extensions

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