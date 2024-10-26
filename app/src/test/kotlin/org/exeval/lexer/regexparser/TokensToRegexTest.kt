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
        val atoms = "abcde".map { RegexToken.Atom(it) }
        val expected = atoms.map { Regex.Atom(it.char) }

        val tokensToRegex = TokensToRegex()
        val actual = atoms.map { token: RegexToken -> tokensToRegex.convert(listOf(token)) }

        assertEquals(expected, actual)
    }

    @Test
    fun `Simple concat work`() {
        val tokens = listOf(RegexToken.Atom('a'), RegexToken.Atom('b'))
        val expected = Regex.Concat(listOf(Regex.Atom('a'), Regex.Atom('b')))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `Simple union work`() {
        val tokens = listOf(RegexToken.Atom('a'), RegexToken.Union, RegexToken.Atom('b'))
        val expected = Regex.Union(setOf(Regex.Atom('a'), Regex.Atom('b')))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `Simple star work`() {
        val tokens = listOf(RegexToken.Atom('a'), RegexToken.Star)
        val expected = Regex.Star(Regex.Atom('a'))

        val tokensToRegex = TokensToRegex()
        val actual = tokensToRegex.convert(tokens)

        assertEquals(expected, actual)
    }

    @Test
    fun `(ab)*|c regex work`() {
        val tokens = listOf(
            RegexToken.OpeningBracket,
            RegexToken.Atom('a'),
            RegexToken.Atom('b'),
            RegexToken.ClosingBracket,
            RegexToken.Star,
            RegexToken.Union,
            RegexToken.Atom('c')
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
            RegexToken.Atom('a'),
            RegexToken.Atom('b'),
            RegexToken.Atom('c'),
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
            RegexToken.Atom('a'),
            RegexToken.Union,
            RegexToken.Atom('b'),
            RegexToken.Union,
            RegexToken.Atom('c'),
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
            RegexToken.Atom('_'),
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
            RegexToken.Atom('t'),
            RegexToken.Atom('r'),
            RegexToken.Atom('u'),
            RegexToken.Atom('e'),
            RegexToken.Union,
            RegexToken.Atom('f'),
            RegexToken.Atom('a'),
            RegexToken.Atom('l'),
            RegexToken.Atom('s'),
            RegexToken.Atom('e'),
            RegexToken.Union,
            RegexToken.OpeningBracket,
            RegexToken.Atom('n'),
            RegexToken.Atom('u'),
            RegexToken.Atom('l'),
            RegexToken.Atom('l'),
            RegexToken.ClosingBracket,
            RegexToken.ClosingBracket,
            RegexToken.OpeningBracket,
            RegexToken.Atom('_'),
            RegexToken.Union,
            RegexToken.Atom('-'),
            RegexToken.ClosingBracket,
            RegexToken.ClosingBracket,
            RegexToken.Star,
            RegexToken.OpeningBracket,
            RegexToken.Atom('m'),
            RegexToken.Atom('a'),
            RegexToken.Atom('t'),
            RegexToken.Atom('r'),
            RegexToken.Atom('i'),
            RegexToken.Atom('x'),
            RegexToken.Union,
            RegexToken.Atom('v'),
            RegexToken.Atom('a'),
            RegexToken.Atom('r'),
            RegexToken.Union,
            RegexToken.Atom('v'),
            RegexToken.Atom('a'),
            RegexToken.Atom('l'),
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