package com.ai.bible.quotation_app.service

import com.ai.bible.quotation_app.model.LLMResponse
import com.ai.bible.quotation_app.model.Scripture
import com.ai.bible.quotation_app.model.Verse
import com.ai.bible.quotation_app.utils.Utils
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class BibleService(private val mongoTemplate: MongoTemplate) {

    fun getScripture(req: LLMResponse?): Scripture {
        val book = req?.book as String
        val chapter = req.chapter as Int
        val verse = req.verse as Int
        val version = req.version as String

        val versionToCollectionMap = mapOf(
            "KJV" to "kjv_verses",
            "NIV" to "niv_verses",
            "NKJV" to "nkjv_verses"
        )

        val hash = Utils.generateHash(book, chapter.toString(), verse.toString())
        val collectionName = versionToCollectionMap[version] ?: "niv_verses"
        val result =  mongoTemplate.findById(hash, Verse::class.java, collectionName) as Verse

        return Scripture(message = result.content)
    }
}