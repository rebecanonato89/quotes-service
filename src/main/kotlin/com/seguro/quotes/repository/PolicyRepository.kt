package com.seguro.quotes.repository

import com.seguro.quotes.domain.model.Policy
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class PolicyRepository {

    private val storage = ConcurrentHashMap<UUID, Policy>()

    fun save(policy: Policy): Policy {
        storage[policy.id] = policy
        return policy
    }

    fun findByQuoteId(quoteId: UUID): Policy? {
        return storage.values.firstOrNull { it.quoteId == quoteId }
    }

    fun findAll(): List<Policy> {
        return storage.values.toList()
    }
}