@file:Suppress("DANGEROUS_CHARACTERS")

package org.exeval.lexer.regexparser

import org.exeval.automata.interfaces.Regex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toSet()
private val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toSet()
private val LETTERS = LOWERCASE union UPPERCASE
private val DIGITS = "0123456789".toSet()
private val IDENTIFIERS = LETTERS union DIGITS union setOf('_')

class TokensToRegexTest {
    @Test
    fun `Atoms work`() {
        val atoms = "abcde".map { RegexToken.RegexChar(it) }
        val expected = atoms.map { Regex.Atom(it.char) }

        val tokensToRegex = TokensToRegex()
        val actual = atoms.map { token: RegexToken -> tokensToRegex.convert(listOf(token)) }

        assertEquals(expected, actual)
    }

    @Test
    fun `Simple concat work`() {
        val tokens = listOf(RegexToken.RegexChar('a'), RegexToken.RegexChar('b'))
        val expected = Regex.Concat(listOf(Regex.Atom('a'), Regex.Atom('b')))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `Simple union work`() {
        val tokens = listOf(RegexToken.RegexChar('a'), RegexToken.Union, RegexToken.RegexChar('b'))
        val expected = Regex.Union(setOf(Regex.Atom('a'), Regex.Atom('b')))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `Simple star work`() {
        val tokens = listOf(RegexToken.RegexChar('a'), RegexToken.Star)
        val expected = Regex.Star(Regex.Atom('a'))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `(ab)*|c regex work`() {
        val tokens = listOf(
            RegexToken.OpeningBracket,
            RegexToken.RegexChar('a'),
            RegexToken.RegexChar('b'),
            RegexToken.ClosingBracket,
            RegexToken.Star,
            RegexToken.Union,
            RegexToken.RegexChar('c')
        )
        val expected =
            Regex.Union(setOf(Regex.Star(Regex.Concat(listOf(Regex.Atom('a'), Regex.Atom('b')))), Regex.Atom('c')))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `(abc) regex work`() {
        val tokens = listOf(
            RegexToken.OpeningBracket,
            RegexToken.RegexChar('a'),
            RegexToken.RegexChar('b'),
            RegexToken.RegexChar('c'),
            RegexToken.ClosingBracket
        )
        val expected = Regex.Concat(listOf(Regex.Atom('a'), Regex.Atom('b'), Regex.Atom('c')))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `(a|b|c) regex work`() {
        val tokens = listOf(
            RegexToken.OpeningBracket,
            RegexToken.RegexChar('a'),
            RegexToken.Union,
            RegexToken.RegexChar('b'),
            RegexToken.Union,
            RegexToken.RegexChar('c'),
            RegexToken.ClosingBracket
        )
        val expected = Regex.Union(setOf(Regex.Atom('a'), Regex.Atom('b'), Regex.Atom('c')))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `Identifier general works`() {
        // test for (\l|_)(\i)* regex is done here
        val tokens = listOf(
            RegexToken.OpeningBracket,
            RegexToken.Group(LOWERCASE),
            RegexToken.Union,
            RegexToken.RegexChar('_'),
            RegexToken.ClosingBracket,
            RegexToken.OpeningBracket,
            RegexToken.Group(IDENTIFIERS),
            RegexToken.ClosingBracket,
            RegexToken.Star
        )
        val expected = Regex.Concat(
            listOf(
                Regex.Union((LOWERCASE union setOf('_')).map { c: Char -> Regex.Atom(c) }.toSet()),
                Regex.Star(Regex.Union(IDENTIFIERS.map { c: Char -> Regex.Atom(c) }.toSet()))
            )
        )

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `Very big and long regex test`() {
        // test for ((true|false|(null))(_|-))*(matrix|var|val)
        val tokens = listOf(
            RegexToken.OpeningBracket,
            RegexToken.OpeningBracket,
            RegexToken.RegexChar('t'),
            RegexToken.RegexChar('r'),
            RegexToken.RegexChar('u'),
            RegexToken.RegexChar('e'),
            RegexToken.Union,
            RegexToken.RegexChar('f'),
            RegexToken.RegexChar('a'),
            RegexToken.RegexChar('l'),
            RegexToken.RegexChar('s'),
            RegexToken.RegexChar('e'),
            RegexToken.Union,
            RegexToken.OpeningBracket,
            RegexToken.RegexChar('n'),
            RegexToken.RegexChar('u'),
            RegexToken.RegexChar('l'),
            RegexToken.RegexChar('l'),
            RegexToken.ClosingBracket,
            RegexToken.ClosingBracket,
            RegexToken.OpeningBracket,
            RegexToken.RegexChar('_'),
            RegexToken.Union,
            RegexToken.RegexChar('-'),
            RegexToken.ClosingBracket,
            RegexToken.ClosingBracket,
            RegexToken.Star,
            RegexToken.OpeningBracket,
            RegexToken.RegexChar('m'),
            RegexToken.RegexChar('a'),
            RegexToken.RegexChar('t'),
            RegexToken.RegexChar('r'),
            RegexToken.RegexChar('i'),
            RegexToken.RegexChar('x'),
            RegexToken.Union,
            RegexToken.RegexChar('v'),
            RegexToken.RegexChar('a'),
            RegexToken.RegexChar('r'),
            RegexToken.Union,
            RegexToken.RegexChar('v'),
            RegexToken.RegexChar('a'),
            RegexToken.RegexChar('l'),
            RegexToken.ClosingBracket,
        )

        val expected = Regex.Concat(
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

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }
}