package org.exeval.lexer.regexparser

import org.exeval.automata.interfaces.Regex
import org.exeval.lexer.interfaces.RegexParser

class RegexParserImpl : RegexParser {
    override fun parse(pattern: String): Regex {
        val tokens = regexTokenizer.tokenize(pattern)
        tokensChecker.check(tokens)
        val regex = tokensToRegex.convert(tokens)

        return regex
    }

    private val regexTokenizer = RegexTokenizer()
    private val tokensChecker = TokenChecker()
    private val tokensToRegex = TokensToRegex()

}