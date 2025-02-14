package com.ai.bible.quotation_app.model

data class LLMResponse(
    val match: Boolean = false,
    val version: String? = null,
    val book: String? = null,
    val chapter: Int? = null,
    val verse: Int? = null,
    val title: String? = null,
)
