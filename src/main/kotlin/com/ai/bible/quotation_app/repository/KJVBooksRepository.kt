package com.ai.bible.quotation_app.repository

import com.ai.bible.quotation_app.model.KJVBook
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface KJVBooksRepository: MongoRepository<KJVBook, String>