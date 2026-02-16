package com.seguro.quotes.domain.model

import com.seguro.quotes.domain.enums.PolicyStatus
import java.time.LocalDate
import java.util.UUID

data class Policy(
    val id: UUID = UUID.randomUUID(),
    val quoteId: UUID,
    val status: PolicyStatus,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate,
    val policyNumber: String
) {
    fun isActive(): Boolean = status == PolicyStatus.ACTIVE && LocalDate.now().isAfter(startDate) && LocalDate.now().isBefore(endDate)
}