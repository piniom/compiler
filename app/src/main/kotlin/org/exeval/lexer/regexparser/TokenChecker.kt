package org.exeval.lexer.regexparser

import org.exeval.lexer.interfaces.BadRegexFormatException
import java.util.*

class TokenChecker {
    fun check(tokens: List<RegexToken>) {
        checkBrackets(tokens)
        checkOperators(tokens)
    }

    private fun checkBrackets(tokens: List<RegexToken>) {
        val openingStack = Stack<Int>()
        for ((i, token) in tokens.withIndex()) {
            when (token) {
                is RegexToken.OpeningBracket -> openingStack.add(i)
                is RegexToken.ClosingBracket -> {
                    if (openingStack.isEmpty()) {
                        throw BadRegexFormatException("Too much brackets at index $i")
                    }
                    openingStack.pop()
                }

                else -> {}
            }
        }
        if (openingStack.isNotEmpty()) {
            throw BadRegexFormatException("Bracket at ${openingStack.peek()} was never closed")
        }
    }

    private fun checkOperators(tokens: List<RegexToken>) {
        if (tokens.first() in setOf(RegexToken.Union, RegexToken.Star)) {
            throw BadRegexFormatException("First symbol is Union or Star")
        }
        if (tokens.last() == RegexToken.Union) {
            throw BadRegexFormatException("Last symbol is Union")
        }
        for (i in 1..<(tokens.size - 1)) {
            val prev = tokens[i - 1]
            val cur = tokens[i]
            val next = tokens[i + 1]
            if (cur == RegexToken.Union) {
                if (prev in setOf(
                        RegexToken.Union,
                        RegexToken.OpeningBracket
                    )
                ) throw BadRegexFormatException("There is illegal combination of operators starting at ${i - 1}")
                if (next in setOf(
                        RegexToken.Union,
                        RegexToken.Star,
                        RegexToken.ClosingBracket
                    )
                ) throw BadRegexFormatException("There is illegal combination of operators starting at $i")
            }
        }
    }
}