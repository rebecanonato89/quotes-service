package com.seguro.quotes.repository

import com.seguro.quotes.domain.model.Quote
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class QuoteRepository {

    // ConcurrentHashMap = thread-safe (múltiplas requisições)
    private val storage = ConcurrentHashMap<UUID, Quote>()

    // Novo: mapear idempotencyKey → quoteId
    private val idempotencyKeys = ConcurrentHashMap<String, UUID>()

    // Salvar com idempotency
    fun saveIdempotent(quote: Quote, idempotencyKey: String?): Quote {
        if (idempotencyKey != null) {
            // Verificar se já existe quote com essa key
            val existingId = idempotencyKeys[idempotencyKey]
            if (existingId != null) {
                // Retornar quote existente (idempotente!)
                return storage[existingId]!!
            }

            // Primeira vez: registrar key
            idempotencyKeys[idempotencyKey] = quote.id
        }

        storage[quote.id] = quote
        return quote
    }

    // Salvar quote
    fun save(quote: Quote): Quote {
        storage[quote.id] = quote
        return quote
    }

    // Buscar por ID
    fun findById(id: UUID): Quote? = storage[id]

    // Listar todas
    fun findAll(): List<Quote> = storage.values.toList()

    // Deletar (para testes)
    fun deleteById(id: UUID): Boolean = storage.remove(id) != null

    // Contar total
    fun count(): Int = storage.size
}