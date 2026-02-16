package com.seguro.quotes.domain.enums


enum class QuoteStatus{
    CREATED,   // Just created
    PRICED,    // Price calculated
    APPROVED,  // Approved
    REJECTED,  // Rejected
    EXPIRED    // Expired
}