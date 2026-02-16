package com.seguro.quotes.dto

import com.seguro.quotes.domain.model.Policy
import com.seguro.quotes.domain.enums.PolicyStatus
import java.time.LocalDate
import java.util.UUID

data class PolicyResponse(
    val id: UUID,
    val quoteId: UUID,
    val policyNumber: String,
    val status: PolicyStatus,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    companion object {
        fun fromPolicy(policy: Policy): PolicyResponse {
            return PolicyResponse(
                id = policy.id,
                quoteId = policy.quoteId,
                policyNumber = policy.policyNumber,
                status = policy.status,
                startDate = policy.startDate,
                endDate = policy.endDate
            )
        }
    }
}