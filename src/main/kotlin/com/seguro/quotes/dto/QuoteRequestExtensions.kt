package com.seguro.quotes.dto

import com.seguro.quotes.common.normalizeDocument
import com.seguro.quotes.common.normalizeName

// Normalizar request (retorna cópia com dados limpos)
fun QuoteRequest.normalized(): QuoteRequest {
    return this.copy(
        name = name.normalizeName(),
        document = document.normalizeDocument(),
        email = email?.trim()?.lowercase(),
        zipCode = zipCode?.normalizeDocument() // remove traços/espaços
    )
}

// Exemplo de uso com apply para logging:
fun QuoteRequest.normalizedWithLog(): QuoteRequest {
    return normalized().apply {
        println("Request normalizado: nome=$name, documento=***")
    }
}