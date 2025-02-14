package com.ai.bible.quotation_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories
class AiBibleQuotationAppApplication

fun main(args: Array<String>) {
    runApplication<AiBibleQuotationAppApplication>(*args)
}
