package com.seguro.quotes.controller

import com.seguro.quotes.common.Either
import com.seguro.quotes.dto.PolicyResponse
import com.seguro.quotes.domain.model.Policy
import com.seguro.quotes.domain.enums.PolicyStatus
import com.seguro.quotes.dto.ErrorResponse
import com.seguro.quotes.service.PolicyService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/policies")
class PolicyController(
    private val policyService: PolicyService
) {

    @PostMapping
    fun issuePolicy(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val quoteId = UUID.fromString(body["quoteId"])

        return when (val result = policyService.issuePolicy(quoteId)) {
            is Either.Right -> ResponseEntity.ok(PolicyResponse.fromPolicy(result.value))
            is Either.Left -> ResponseEntity
                .badRequest()
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }

    @GetMapping("/{id}")
    fun getPolicyById(@PathVariable id: UUID): ResponseEntity<*> {
        return when (val result = policyService.getPolicyById(id)) {
            is Either.Right -> ResponseEntity.ok(PolicyResponse.fromPolicy(result.value))
            is Either.Left -> ResponseEntity
                .badRequest()
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }

    @GetMapping
    fun listAllPolicies(): ResponseEntity<List<PolicyResponse>> {
        val policies = policyService.listAllPolicies()
        return ResponseEntity.ok(policies.map { PolicyResponse.fromPolicy(it) })
    }
}