package com.seguro.quotes.domain.enums


enum class Coverage(val additionalCost: Double) {
    THEFT(20.0),
    COLLISION(30.0),
    ASSISTANCE(10.0),
    THIRD_PARTY_DAMAGE(25.0)
}