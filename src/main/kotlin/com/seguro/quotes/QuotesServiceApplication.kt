package com.seguro.quotes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QuotesServiceApplication

fun main(args: Array<String>) {
    runApplication<QuotesServiceApplication>(*args)
}

