package com.ai.bible.quotation_app.config

import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


private val log = KotlinLogging.logger {}

@Configuration
class LLMConfig {

    @Bean
    fun chatModelListener(): ChatModelListener {
        return object : ChatModelListener {

            override fun onRequest(requestContext: ChatModelRequestContext) {
                log.info { "onRequest(): ${requestContext.chatRequest()}" }
            }

            override fun onResponse(responseContext: ChatModelResponseContext) {
                log.info { "onResponse(): ${responseContext.chatRequest()}" }
            }

            override fun onError(errorContext: ChatModelErrorContext) {
                log.info { "onError(): ${errorContext.error()}" }
            }
        }
    }

    @Bean
    fun chatMemoryProvider() = ChatMemoryProvider { memoryId: Any? ->
        MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(20)
            .build()
    }
}