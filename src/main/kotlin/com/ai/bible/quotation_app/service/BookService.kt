package com.ai.bible.quotation_app.service

import com.ai.bible.quotation_app.model.LLMResponse
import com.ai.bible.quotation_app.repository.KJVBooksRepository
import com.ai.bible.quotation_app.repository.NIVBooksRepository
import com.ai.bible.quotation_app.repository.NKJVBooksRepository
import org.springframework.stereotype.Service

@Service
class BookService(
    private val kjvBooksRepository: KJVBooksRepository,
    private val nivBooksRepository: NIVBooksRepository,
    private val nkjvBooksRepository: NKJVBooksRepository
) {
    fun saveB(req: LLMResponse): String {
        return "hello world"
    }
}