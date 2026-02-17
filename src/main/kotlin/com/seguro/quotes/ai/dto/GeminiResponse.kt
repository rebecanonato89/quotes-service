package com.seguro.quotes.ai.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// ---------- Success response ----------
@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiCandidate(
    val content: GeminiContentResponse? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiContentResponse(
    val parts: List<GeminiPartResponse>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiPartResponse(
    val text: String? = null
)

// ---------- Error response (matches the 429 payload you posted) ----------
@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiErrorResponse(
    val error: GeminiErrorDetail? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiErrorDetail(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null,
    val details: List<GeminiErrorDetailInfo>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiErrorDetailInfo(
    @JsonProperty("@type")
    val type: String? = null,

    // Help object sometimes contains links
    val links: List<GeminiHelpLink>? = null,

    // QuotaFailure object contains violations[]
    val violations: List<GeminiQuotaViolation>? = null,

    // RetryInfo object contains retryDelay
    val retryDelay: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiHelpLink(
    val description: String? = null,
    val url: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiQuotaViolation(
    val quotaMetric: String? = null,
    val quotaId: String? = null,
    val quotaDimensions: Map<String, String>? = null
)
