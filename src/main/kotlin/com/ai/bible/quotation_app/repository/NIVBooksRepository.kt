package com.ai.bible.quotation_app.repository

import com.ai.bible.quotation_app.model.NIVBook
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface NIVBooksRepository: MongoRepository<NIVBook, String>