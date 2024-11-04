package org.exeval.utilities

import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.TokenCategories

class LexerUtils {
    companion object {
        fun removeWhitespaceTokens(tokens: List<LexerToken>): List<LexerToken> {
            return tokens.filter { it.categories != setOf(TokenCategories.Whitespace) }
        }
    }
}
