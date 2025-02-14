package com.ai.bible.quotation_app.utils

import java.security.MessageDigest

class Utils {
    companion object {
        fun generateHash(book: String, chapter: String, verse: String): String {
            val input = "$book|$chapter|$verse"
            return MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray())
                .fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}
