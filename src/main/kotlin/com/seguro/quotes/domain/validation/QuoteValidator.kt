package com.seguro.quotes.domain.validation

import com.seguro.quotes.common.Either
import com.seguro.quotes.common.DomainError
import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.domain.enums.InsuranceType

object QuoteValidator {

    // Validação completa (encadeia todas)
    fun validate(request: QuoteRequest): Either<DomainError, QuoteRequest> {
        return validateDocument(request.document)
            .flatMap { validateEmail(request.email) }
            .flatMap { validateInsuranceSpecificRules(request) }
            .map { request } // Se tudo OK, retorna request
    }

    // Valida documento (não vazio)
    private fun validateDocument(document: String): Either<DomainError, String> {
        return if (document.isNotBlank()) {
            Either.Right(document)
        } else {
            Either.Left(DomainError.InvalidDocument)
        }
    }

    // Valida email (se fornecido, deve ter @)
    private fun validateEmail(email: String?): Either<DomainError, String?> {
        return when {
            email == null -> Either.Right(null) // OK, é opcional
            email.contains("@") -> Either.Right(email)
            else -> Either.Left(DomainError.InvalidEmail)
        }
    }

    // Valida regras específicas por tipo de seguro
    private fun validateInsuranceSpecificRules(
        request: QuoteRequest
    ): Either<DomainError, QuoteRequest> {
       return when (request.insuranceType) {
            InsuranceType.AUTO -> validateAutoRules(request)
            InsuranceType.LIFE -> validateLifeRules(request)
            InsuranceType.HOME -> validateHomeRules(request)

        }
    }

    // AUTO: veículo obrigatório
    private fun validateAutoRules(request: QuoteRequest): Either<DomainError, QuoteRequest> {
        val veiculo = request.vehicle
            ?: return Either.Left(DomainError.MissingVehicle)

        return if (veiculo.isValid()) {
            Either.Right(request)
        } else {
            Either.Left(DomainError.InvalidVehicle)
        }
    }

    // VIDA: idade obrigatória e >= 18
    private fun validateLifeRules(request: QuoteRequest): Either<DomainError, QuoteRequest> {
        val idade = request.age
            ?: return Either.Left(DomainError.MissingAge)

        return if (idade >= 18) {
            Either.Right(request)
        } else {
            Either.Left(DomainError.Underage)
        }
    }

    // HOME: zip code obrigatório
    private fun validateHomeRules(request: QuoteRequest): Either<DomainError, QuoteRequest>
    {
        val zip = request.zipCode
            ?: return Either.Left(DomainError.MissingZipCode)

        return if (zip.isNotBlank()) {
            Either.Right(request)
        } else {
            Either.Left(DomainError.InvalidZipCode)
        }
    }
}