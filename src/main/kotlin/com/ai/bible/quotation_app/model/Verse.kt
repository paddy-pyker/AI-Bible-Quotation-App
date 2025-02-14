package com.ai.bible.quotation_app.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Verse(
    @Id
    val hash: String,
    var book: String,
    var chapter: Int,
    var verseNumber: Int,
    var content: String,
)
