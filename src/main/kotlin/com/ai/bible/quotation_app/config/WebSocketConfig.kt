package com.ai.bible.quotation_app.config

import com.ai.bible.quotation_app.component.AudioStreamHandler
import com.ai.bible.quotation_app.component.TranscribeAudio
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(private val transcribeAudio: TranscribeAudio) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(webSocketHandler(), "/audioStream").setAllowedOrigins("*")
    }

    @Bean
    fun webSocketHandler() = AudioStreamHandler(transcribeAudio)
}