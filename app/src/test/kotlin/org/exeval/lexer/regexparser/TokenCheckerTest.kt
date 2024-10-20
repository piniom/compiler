package org.exeval.lexer.regexparser

import org.exeval.lexer.interfaces.BadRegexFormatException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TokenCheckerTest {
    @Test
    fun `Good brackets`() {
        val tests = listOf("()", "(())", "()()", "(())()()", "(((()())(())))").map { s: String ->
            s.map { c: Char ->
                when (c) {
                    '(' -> RegexToken.OpeningBracket
                    ')' -> RegexToken.ClosingBracket
                    else -> RegexToken.RegexChar(' ')
                }
            }
        }

        val tokenChecker = TokenChecker()
        for (test in tests) {
            assertDoesNotThrow {
                tokenChecker.check(test)
            }
        }
    }

    @Test
    fun `Bad brackets`() {
        val tests = listOf("(", ")", "())", "()(", "(())(()", "(((()())(()))").map { s: String ->
            s.map { c: Char ->
                when (c) {
                    '(' -> RegexToken.OpeningBracket
                    ')' -> RegexToken.ClosingBracket
                    else -> RegexToken.RegexChar(' ')
                }
            }
        }

        val tokenChecker = TokenChecker()
        for (test in tests) {
            assertThrows<BadRegexFormatException> {
                tokenChecker.check(test)
            }
        }
    }

    @Test
    fun `Good operators`() {
        val tests = listOf(
            listOf(
                RegexToken.RegexChar('a'),
                RegexToken.Union,
                RegexToken.RegexChar('b')
            ),
            listOf(
                RegexToken.OpeningBracket,
                RegexToken.RegexChar('a'),
                RegexToken.RegexChar('b'),
                RegexToken.ClosingBracket,
                RegexToken.Star,
                RegexToken.Union,
                RegexToken.RegexChar('c')
            )
        )

        val tokenChecker = TokenChecker()
        for (test in tests) {
            assertDoesNotThrow {
                tokenChecker.check(test)
            }
        }
    }

    @Test
    fun `Bad operators`() {
        val tests = listOf(
            listOf(
                RegexToken.Union,
                RegexToken.Union
            ),
            listOf(
                RegexToken.OpeningBracket,
                RegexToken.Union,
                RegexToken.ClosingBracket
            ),
            listOf(
                RegexToken.OpeningBracket,
                RegexToken.ClosingBracket,
                RegexToken.Union,
                RegexToken.Star,
            ),
        )

        val tokenChecker = TokenChecker()
        for (test in tests) {
            assertThrows<BadRegexFormatException> {
                tokenChecker.check(test)
            }
        }

    }
}