package com.seguro.quotes.controller

import com.seguro.quotes.common.Either
import com.seguro.quotes.domain.validation.QuoteValidator
import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.dto.QuoteResponse
import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.domain.extensions.toSafeLogString
import com.seguro.quotes.domain.service.PriceCalculator
import com.seguro.quotes.dto.ErrorResponse
import com.seguro.quotes.dto.normalized
import com.seguro.quotes.repository.QuoteRepository
import com.seguro.quotes.service.QuoteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/quotes")
class QuoteController(private val quoteService: QuoteService) {

    // POST /quotes - Create quote (stub)
    @PostMapping
    suspend fun createQuote(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<*> {
        return when (val result = quoteService.createQuote(request)) {
            is Either.Right -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(QuoteResponse.from(result.value))

            is Either.Left -> ResponseEntity
                .badRequest()
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }

    @PostMapping("/async")
    fun createQuoteSync(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<*> {
        return when (val result = quoteService.createQuoteAsync(request)) {
            is Quote -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(QuoteResponse.from(result))

            else -> ResponseEntity
                .badRequest()
                .body(ErrorResponse("UNKNOWN_ERROR", "An unknown error occurred"))
        }
    }


    // GET /quotes/{id} - Get quote
    @GetMapping("/{id}")
    fun getQuote(@PathVariable id: UUID): ResponseEntity<*> {
        return when (val result = quoteService.getQuoteById(id)) {
            is Either.Right -> ResponseEntity.ok(QuoteResponse.from(result.value))
            is Either.Left -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }

    @GetMapping
    fun listQuotes(): ResponseEntity<List<QuoteResponse>> {
        val quotes = quoteService.listAllQuotes()
        val responses = quotes.map { QuoteResponse.from(it) }
        return ResponseEntity.ok(responses)
    }
}