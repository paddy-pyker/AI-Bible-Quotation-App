package com.ai.bible.quotation_app.component

import com.ai.bible.quotation_app.model.LLMResponse
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.spring.AiService

@AiService
interface Gemini {

    @SystemMessage(fromResource = "models/prompt-template.txt")

    fun chat(@MemoryId memoryId: String, @UserMessage userMessage: String?): LLMResponse?
}