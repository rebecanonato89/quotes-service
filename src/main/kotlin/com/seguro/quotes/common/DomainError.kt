package com.seguro.quotes.common

// Sealed class: todos os erros possíveis conhecidos em tempo de compilação
sealed class DomainError(val message: String, val code: String) {

    // Erros de validação
    object InvalidDocument : DomainError(
        "Documento é obrigatório e não pode ser vazio",
        "INVALID_DOCUMENT"
    )

    object InvalidEmail : DomainError(
        "Email deve conter @",
        "INVALID_EMAIL"
    )

    object MissingVehicle : DomainError(
        "Veículo é obrigatório para seguro AUTO",
        "MISSING_VEHICLE"
    )

    object InvalidVehicle : DomainError(
        "Veículo deve ter placa OU modelo",
        "INVALID_VEHICLE"
    )

    object MissingAge : DomainError(
        "Idade é obrigatória para seguro VIDA",
        "MISSING_AGE"
    )

    object MissingZipCode : DomainError(
        "Zip code é obrigatório para seguro HOME",
        "MISSING_ZIP_CODE"
    )

    object Underage : DomainError(
        "Idade mínima: 18 anos",
        "UNDERAGE"
    )

    // Erros de negócio
    object QuoteNotFound : DomainError(
        "Cotação não encontrada",
        "QUOTE_NOT_FOUND"
    )

    object QuoteNotApproved : DomainError(
        "Cotação não está aprovada para emitir apólice",
        "QUOTE_NOT_APPROVED"
    )

    object QuoteExpired : DomainError(
        "Cotação expirada (7 dias sem emissão)",
        "QUOTE_EXPIRED"
    )

    object InvalidZipCode : DomainError(
        "Zip code deve conter 5 dígitos",
        "INVALID_ZIP_CODE"
    )

    data class LimitExceeded(val limit: Double, val actual: Double) : DomainError(
        "Preço $actual excede limite de $limit",
        "LIMIT_EXCEEDED"
    )
}