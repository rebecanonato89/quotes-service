package com.seguro.quotes.service

import com.seguro.quotes.common.Either
import com.seguro.quotes.common.DomainError
import com.seguro.quotes.domain.model.Policy
import com.seguro.quotes.domain.enums.PolicyStatus
import com.seguro.quotes.infrastructure.event.EventPublisher
import com.seguro.quotes.domain.event.toIssuedEvent
import com.seguro.quotes.repository.QuoteRepository
import com.seguro.quotes.repository.PolicyRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class PolicyService(
    private val policyRepository: PolicyRepository,
    private val quoteRepository: QuoteRepository,
    private val eventPublisher: EventPublisher
) {

    // Emitir apólice a partir de quote
    fun issuePolicy(quoteId: UUID): Either<DomainError, Policy> {
        // 1. Buscar quote
        val quote = quoteRepository.findById(quoteId)
            ?: return Either.Left(DomainError.QuoteNotFound)

        // 2. Validar se pode emitir
        if (!quote.canGeneratePolicy()) {
            return Either.Left(DomainError.QuoteNotApproved)
        }

        if (quote.isExpired()) {
            return Either.Left(DomainError.QuoteExpired)
        }

        // 3. Criar policy
        val policy = Policy(
            quoteId = quoteId,
            status = PolicyStatus.ACTIVE,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusYears(1),
            policyNumber = "POL-%s-%d-%d".format(
                quote.request.insuranceType,
                System.currentTimeMillis(),
                (1000..9999).random()
            )
        )

        // 4. Persistir
        val savedPolicy = policyRepository.save(policy)

        eventPublisher.publish(savedPolicy.toIssuedEvent())

        // 5. Log
        println("Policy emitida: id=${savedPolicy.id}, quoteId=$quoteId")

        return Either.Right(savedPolicy)
    }

    // Buscar policy por ID
    fun getPolicyById(id: UUID): Either<DomainError, Policy> {
        // TODO: Implemente (similar a getQuoteById)
        TODO("Implementar busca de policy")
    }

    // Listar todas
    fun listAllPolicies(): List<Policy> = policyRepository.findAll()
}