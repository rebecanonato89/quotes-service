package com.seguro.quotes.config

import com.seguro.quotes.domain.event.*
import com.seguro.quotes.infrastructure.event.EventPublisher
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventConfig {

    // Registrar listeners na inicialização da aplicação
    @Bean
    fun eventListenersSetup(eventPublisher: EventPublisher) = CommandLineRunner {

        // Listener 1: Quote aprovada
        eventPublisher.subscribe { event ->
            if (event is QuoteApprovedEvent) {
                println("📬 Evento recebido: Quote ${event.quoteId} aprovada por R$ ${event.price}")
                // Futuro: enviar email, notificar outro serviço, etc.
            }
        }

        // Listener 2: Quote rejeitada
        eventPublisher.subscribe { event ->
            if (event is QuoteRejectedEvent) {
                println("❌ Evento recebido: Quote ${event.quoteId} rejeitada - ${event.reasons}")
                // Futuro: enviar email de rejeição
            }
        }

        // Listener 3: Policy emitida
        eventPublisher.subscribe { event ->
            if (event is PolicyIssuedEvent) {
                println("🎉 Evento recebido: Policy ${event.policyNumber} emitida!")
                // Futuro: enviar apólice por email, gerar PDF, etc.
            }
        }

        println("✅ Event listeners registrados")
    }
}