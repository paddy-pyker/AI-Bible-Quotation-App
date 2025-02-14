package com.ai.bible.quotation_app.model

import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = "nkjv_books")
class NKJVBook : Book()