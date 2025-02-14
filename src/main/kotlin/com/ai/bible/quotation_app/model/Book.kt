package com.ai.bible.quotation_app.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

abstract class Book(
    @Id
    @JsonIgnore
    var id: ObjectId? = null,
    var title: String? = null,
    var content: String? = null,
)
