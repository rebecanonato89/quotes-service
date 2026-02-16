package com.seguro.quotes.infrastructure.event

import com.seguro.quotes.domain.event.DomainEvent
import org.springframework.stereotype.Component

// Type alias: listener é função que recebe evento
typealias EventListener = (DomainEvent) -> Unit

@Component
class EventPublisher {

    // Lista de listeners registrados
    private val listeners = mutableListOf<EventListener>()

    // Registrar listener
    fun subscribe(listener: EventListener) {
        listeners.add(listener)
    }

    // Publicar evento (notifica todos os listeners)
    fun publish(event: DomainEvent) {
        listeners.forEach { listener ->
            try {
                listener(event) // Chama lambda
            } catch (e: Exception) {
                println("Erro ao processar evento: ${e.message}")
            }
        }
    }
}