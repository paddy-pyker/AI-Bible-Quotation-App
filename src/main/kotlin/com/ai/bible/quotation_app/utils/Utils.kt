package com.ai.bible.quotation_app.utils

import net.openhft.hashing.LongHashFunction

class Utils {
    companion object {
        fun generateHash(book: String, chapter: String, verse: String): String {
            val input = "$book|$chapter|$verse"

            // Compute the XXH3 64-bit hash.
            val hashValue: Long = LongHashFunction.xx3().hashChars(input)

            // Convert to an unsigned hexadecimal string (16 hex digits for 64-bit).
            return java.lang.Long.toUnsignedString(hashValue, 16).padStart(16, '0')
        }
    }
}
