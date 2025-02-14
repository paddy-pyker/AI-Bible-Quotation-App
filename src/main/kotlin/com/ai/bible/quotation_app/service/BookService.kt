package com.ai.bible.quotation_app.service

import com.ai.bible.quotation_app.model.LLMResponse
import org.springframework.stereotype.Service

@Service
class BookService(
) {
    fun saveB(req: LLMResponse): String {
        return "hello world"
    }
}