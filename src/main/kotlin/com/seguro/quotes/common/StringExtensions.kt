package com.seguro.quotes.common

// Mascarar documento (para logs sanitizados)
fun String.maskDocument(): String {
    if (this.length < 4) return "***"

    return this.takeLast(4).let { last4 ->
        "*".repeat(this.length - 4) + last4
    }
}

// Exemplos:
// "12345678900".maskDocument() → "*******8900"
// "123".maskDocument() → "***"

// Normalizar documento (remover pontos, traços, espaços)
fun String.normalizeDocument(): String {
    return this.replace(Regex("[^0-9]"), "")
}

// Exemplos:
// "123.456.789-00".normalizeDocument() → "12345678900"
// "123 456 789 00".normalizeDocument() → "12345678900"

// Normalizar nome (trim, capitalizar primeira letra de cada palavra)
fun String.normalizeName(): String {
    return this.trim()
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}

// Exemplos:
// "  joÃO   siLVA  ".normalizeName() → "João Silva"
// "MARIA".normalizeName() → "Maria"