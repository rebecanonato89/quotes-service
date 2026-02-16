package com.seguro.quotes.service

import com.seguro.quotes.common.Either
import com.seguro.quotes.common.DomainError
import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.domain.validation.QuoteValidator
import com.seguro.quotes.domain.service.PriceCalculator
import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.dto.normalized
import com.seguro.quotes.repository.QuoteRepository
import com.seguro.quotes.domain.extensions.toSafeLogString
import com.seguro.quotes.domain.extensions.toApprovedEvent
import com.seguro.quotes.domain.extensions.toRejectedEvent
import com.seguro.quotes.infrastructure.event.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class QuoteService(
    private val quoteRepository: QuoteRepository,
    private val riskScoreService: RiskScoreService,
    private val coroutineScope: CoroutineScope, // injetado
    private val eventPublisher: EventPublisher //
) {

    // Criar quote em background
    fun createQuoteAsync(request: QuoteRequest): Quote {
        // 1. Criar quote com status CREATED
        val quote = Quote(
            status = QuoteStatus.CREATED,
            price = null, // Ainda não calculado
            request = request.normalized()
        )

        val savedQuote = quoteRepository.save(quote)

        // 2. Processar assíncrono (lançar coroutine)
        coroutineScope.launch {
            processQuoteAsync(savedQuote.id, request)
        }

        return savedQuote
    }

    // Processar em background
    private suspend fun processQuoteAsync(quoteId: UUID, request: QuoteRequest) {
        try {
            // Calcular risk score
            val riskScore = riskScoreService.calculateRiskScore(request.document)

            // Calcular preço
            val priceResult = PriceCalculator.calculate(request)
            val adjustedPrice = if (riskScore > 70) priceResult.price * 1.10 else priceResult.price

            // Atualizar quote
            val updatedQuote = quoteRepository.findById(quoteId)?.copy(
                status = if (adjustedPrice <= 300.0) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
                price = adjustedPrice,
                rejectionReasons = if (adjustedPrice > 300.0) listOf("LIMIT_EXCEEDED") else emptyList()
            ) ?: run {
                println("[processQuoteAsync] Quote $quoteId not found for update.")
                return
            }

            val savedQuote = quoteRepository.save(updatedQuote)

            try {
                when {
                    savedQuote.canGeneratePolicy() -> {
                        val event = savedQuote.toApprovedEvent()
                        if (event != null) {
                            eventPublisher.publish(event)
                            println("[processQuoteAsync] Published ApprovedEvent for quote $quoteId")
                        } else {
                            println("[processQuoteAsync] toApprovedEvent returned null for quote $quoteId")
                        }
                    }
                    savedQuote.status == QuoteStatus.REJECTED -> {
                        val event = savedQuote.toRejectedEvent()
                        if (event != null) {
                            eventPublisher.publish(event)
                            println("[processQuoteAsync] Published RejectedEvent for quote $quoteId")
                        } else {
                            println("[processQuoteAsync] toRejectedEvent returned null for quote $quoteId")
                        }
                    }
                    else -> {
                        println("[processQuoteAsync] No event to publish for quote $quoteId with status ${savedQuote.status}")
                    }
                }
            } catch (e: Exception) {
                println("[processQuoteAsync] Error publishing event for quote $quoteId: ${e.message}")
            }
        } catch (e: Exception) {
            // Marcar como erro
            println("[processQuoteAsync] Erro ao processar quote $quoteId: ${e.message}")
        }
    }

    suspend fun createQuote(request: QuoteRequest): Either<DomainError, Quote> {
        // 1. Normalizar
        val normalizedRequest = request.normalized()

        // 2. Validar
        val validation = QuoteValidator.validate(normalizedRequest)
        if (validation.isLeft) {
            return validation as Either.Left<DomainError>
        }

        // 3. Calcular risk score (assíncrono!)
        val riskScore = riskScoreService.calculateRiskScore(normalizedRequest.document)
        println("Risk score: $riskScore")

        // 4. Calcular preço
        val priceResult = PriceCalculator.calculate(normalizedRequest)

        // 5. Ajustar preço baseado em risk (exemplo)
        val adjustedPrice = if (riskScore > 70) {
            priceResult.price * 1.10 // +10% para alto risco
        } else {
            priceResult.price
        }

        // 6. Criar quote
        val quote = Quote(
            status = if (adjustedPrice <= 300.0) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
            price = adjustedPrice,
            rejectionReasons = if (adjustedPrice > 300.0) listOf("LIMIT_EXCEEDED") else emptyList(),
            request = normalizedRequest
        )

        // 7. Persistir
        val savedQuote = quoteRepository.save(quote)

        try {
            when {
                savedQuote.canGeneratePolicy() -> {
                    val event = savedQuote.toApprovedEvent()
                    if (event != null) {
                        eventPublisher.publish(event)
                        println("[createQuote] Published ApprovedEvent for quote ${savedQuote.id}")
                    } else {
                        println("[createQuote] toApprovedEvent returned null for quote ${savedQuote.id}")
                    }
                }
                savedQuote.status == QuoteStatus.REJECTED -> {
                    val event = savedQuote.toRejectedEvent()
                    if (event != null) {
                        eventPublisher.publish(event)
                        println("[createQuote] Published RejectedEvent for quote ${savedQuote.id}")
                    } else {
                        println("[createQuote] toRejectedEvent returned null for quote ${savedQuote.id}")
                    }
                }
                else -> {
                    println("[createQuote] No event to publish for quote ${savedQuote.id} with status ${savedQuote.status}")
                }
            }
        } catch (e: Exception) {
            println("[createQuote] Error publishing event for quote ${savedQuote.id}: ${e.message}")
        }

        println(savedQuote.toSafeLogString())

        return Either.Right(savedQuote)
    }

    // Buscar quote por ID
    fun getQuoteById(id: UUID): Either<DomainError, Quote> {
        val quote = quoteRepository.findById(id) //Evitar !! (force unwrap)
            ?: return Either.Left(DomainError.QuoteNotFound) //tratamento explícito de erro

        return Either.Right(quote)
    }

    // Listar todas
    fun listAllQuotes(): List<Quote> = quoteRepository.findAll()
}