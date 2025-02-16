package com.ai.bible.quotation_app.component

import com.ai.bible.quotation_app.model.Verse
import com.ai.bible.quotation_app.utils.Utils
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class SeedDB(
    private val mongoTemplate: MongoTemplate
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val translations = listOf("KJV", "NIV", "NKJV")
        val collectionNames = listOf("kjv_verses", "niv_verses", "nkjv_verses")

        translations.forEachIndexed { index, translation ->
            val collectionName = collectionNames[index]
            if (isCollectionEmpty(collectionName)) {
                loadBookDataFromFiles(translation)
                    .forEach { verse -> mongoTemplate.save(verse, collectionName) }
            }
        }

        log.info { "application is ready" }

    }

    private fun isCollectionEmpty(collectionName: String): Boolean {
        val count = mongoTemplate.getCollection(collectionName).countDocuments()
        return count == 0L
    }

    private fun loadBookDataFromFiles(s: String): List<Verse> {
        log.info { "About to load $s into the database" }
        //transforming data first
        val mapper = ObjectMapper()
        val inputStream = object {}.javaClass.getResourceAsStream("/bibleTranslations/${s}_bible.json")
        val data = mapper.readValue(inputStream, object : TypeReference<Map<String, Any>>() {})

        return data?.entries?.flatMap { (bookName, chapters) ->
            (chapters as? Map<String, Map<String, String>>)?.entries?.flatMap { (chapter, verses) ->
                verses.entries.map { (verseNumber, content) ->
                    Verse(
                        book = bookName,
                        chapter = chapter.toInt(),
                        verseNumber = verseNumber.toInt(),
                        content = content,
                        hash = Utils.generateHash(bookName, chapter, verseNumber)
                    )
                }
            } ?: emptyList()
        } ?: emptyList()
    }
}