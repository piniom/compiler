package org.exeval.utilities

import org.exeval.parser.grammar.GrammarSymbol
import org.exeval.parser.grammar.LanguageGrammar
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.parser.interfaces.ParseTree

class UnambiguesTokenCategories(token: LexerToken): Exception("Token has categories of equal priority: " + token)

class LexerUtils {
    companion object {

        private fun removeWhitespaceTokens(tokens: List<LexerToken>): List<LexerToken> {
            return tokens.filter { it.categories != setOf(TokenCategories.Whitespace) }
        }

        fun lexerTokensToParseTreeLeaves(tokens: List<LexerToken>): List<ParseTree.Leaf<GrammarSymbol>> {
            val leaves = mutableListOf<ParseTree.Leaf<GrammarSymbol>>()
            for (token in removeWhitespaceTokens(tokens)) {
                val newCategories = token.categories - TokenCategories.IdentifierNontype
                // IdentifierNontype has always the lowest priority
                var category = TokenCategories.IdentifierNontype
                if(newCategories.size == 1) {
                    category = newCategories.first() as TokenCategories
                } 
                else if (newCategories.size == 2 && newCategories.contains(TokenCategories.LiteralNothing) && newCategories.contains(TokenCategories.OperatorNot)) {
                    // 'not' is substring of 'nothing' so if it is in categories, 'nothing' wins over 'not' keyword
                    category = TokenCategories.LiteralNothing
                } else if(newCategories.size > 1) {
                    throw UnambiguesTokenCategories(token)
                }
                leaves.add(ParseTree.Leaf(category, token.startLocation, token.stopLocation))
            }

            // add last endOfParse to leaves
            val lastLocation = leaves.last().endLocation
            leaves.add(ParseTree.Leaf(LanguageGrammar.grammar.endOfParse, lastLocation, lastLocation))

            return leaves
        }
    }
}
