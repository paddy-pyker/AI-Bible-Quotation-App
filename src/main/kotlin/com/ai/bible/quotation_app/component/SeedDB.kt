package com.ai.bible.quotation_app.component

import com.ai.bible.quotation_app.repository.KJVBooksRepository
import com.ai.bible.quotation_app.repository.NIVBooksRepository
import com.ai.bible.quotation_app.repository.NKJVBooksRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class SeedDB(
    private val kjvBooksRepository: KJVBooksRepository,
    private val nivBooksRepository: NIVBooksRepository,
    private val nkjvBooksRepository: NKJVBooksRepository
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if(kjvBooksRepository.count().toInt() == 0 ){
            loadBookDataFromFiles("KJV")
        }

        if(nivBooksRepository.count().toInt() == 0 ){
            loadBookDataFromFiles("NIV")
        }

        if(nkjvBooksRepository.count().toInt() == 0 ){
            loadBookDataFromFiles("NKJV")
        }
    }

    private fun loadBookDataFromFiles(s: String) {
        log.info { "About to load $s into the database" }
            //transforming -> loading
    }
}