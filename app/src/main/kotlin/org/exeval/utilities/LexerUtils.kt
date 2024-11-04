package org.exeval.utilities

import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.TokenCategories
import org.exeval.parser.interfaces.ParseTree

class UnambiguesTokenCategories(token: LexerToken): Exception("Token has categories of equal priority: " + token)

class LexerUtils {
    companion object {

        private fun removeWhitespaceTokens(tokens: List<LexerToken>): List<LexerToken> {
            return tokens.filter { it.categories != setOf(TokenCategories.Whitespace) }
        }

        fun lexerTokensToParseTreeLeaves(tokens: List<LexerToken>): List<ParseTree.Leaf<TokenCategories>> {
            var leaves = mutableListOf<ParseTree.Leaf<TokenCategories>>()
            for (token in removeWhitespaceTokens(tokens)) {
                val newCategories = token.categories - TokenCategories.IdentifierNontype
                // IdentifierNontype has always the lowest priority
                var category = TokenCategories.IdentifierNontype
                if(newCategories.size == 1) {
                    category = newCategories.first() as TokenCategories
                } else if(newCategories.size > 1) {
                    throw UnambiguesTokenCategories(token)
                }
                leaves.add(ParseTree.Leaf(category, token.startLocation, token.stopLocation))
            }

            return leaves
        }
    }
}
