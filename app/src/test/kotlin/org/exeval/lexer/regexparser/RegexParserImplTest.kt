package org.exeval.lexer.regexparser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.exeval.automata.interfaces.Regex
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegexParserImplTest {
    @Test
    fun `RegexParser correctly passes tokens`() {
        val tokenizer = mockk<RegexTokenizer>()
        val checker = mockk<TokenChecker>(relaxUnitFun = true)
        val tokensToRegex = mockk<TokensToRegex>()
        val regexParser = RegexParserImpl(tokenizer, checker, tokensToRegex)

        val pattern = "(l)"
        val tokens = listOf(RegexToken.Atom('l'))
        val regex = Regex.Atom('l')

        every { tokenizer.tokenize(pattern) } returns tokens
        every { tokensToRegex.convert(tokens) } returns regex

        assertEquals(regex, regexParser.parse(pattern))
        verify { tokenizer.tokenize(pattern) }
        verify { checker.check(tokens) }
        verify { tokensToRegex.convert(tokens) }
    }

    @Test
    fun `Integration tests for some regexes`() {
        val kLowercase = "abcdefghijklmnopqrstuvwxyz".toSet()
        val kUpperCases = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toSet()
        val kLetters = kLowercase union kUpperCases
        val kDigits = "0123456789".toSet()
        val kIdentifiers = kLetters union kDigits union setOf('_')

        val inputs = listOf("""(a|b|c)""", """(\l|_)(\i)*""", """((true|false|(null))(_|-))*(matrix|var|val)""")
        val expected = listOf(
            Regex.Union(setOf(Regex.Atom('a'), Regex.Atom('b'), Regex.Atom('c'))),
            Regex.Concat(
                listOf(
                    Regex.Union((kLowercase union setOf('_')).map { c: Char -> Regex.Atom(c) }.toSet()),
                    Regex.Star(Regex.Union(kIdentifiers.map { c: Char -> Regex.Atom(c) }.toSet()))
                )
            ),
            Regex.Concat(
                listOf(
                    Regex.Star(
                        Regex.Concat(
                            listOf(
                                Regex.Union(
                                    setOf(
                                        Regex.Concat("true".map { c: Char -> Regex.Atom(c) }),
                                        Regex.Concat("false".map { c: Char -> Regex.Atom(c) }),
                                        Regex.Concat("null".map { c: Char -> Regex.Atom(c) })
                                    )
                                ), Regex.Union(
                                    "_-".map { c: Char -> Regex.Atom(c) }.toSet()
                                )
                            )
                        )
                    ), Regex.Union(
                        setOf(
                            Regex.Concat("matrix".map { c: Char -> Regex.Atom(c) }),
                            Regex.Concat("var".map { c: Char -> Regex.Atom(c) }),
                            Regex.Concat("val".map { c: Char -> Regex.Atom(c) })
                        )
                    )
                )
            )
        )

        val regexTokenizer = RegexTokenizer()
        val tokensChecker = TokenChecker()
        val tokensToRegex = TokensToRegex()
        val regexParserImpl = RegexParserImpl(regexTokenizer, tokensChecker, tokensToRegex)

        val actual = inputs.map { regexParserImpl.parse(it) }

        assertEquals(expected, actual)
    }
}