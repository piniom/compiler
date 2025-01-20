package org.exeval.lexer.regexparser

import org.exeval.lexer.interfaces.BadRegexFormatException
import org.exeval.lexer.regexparser.RegexToken.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RegexTokenizerTest {
	@Test
	fun `Simple strings work`() {
		val strings =
			listOf(
				"abcde",
				"AbcDe",
				"Something about 123",
				"What about other sym-bols/characters",
			)
		val expected = strings.map { s: String -> s.map { c: Char -> Atom(c) } }

		val regexTokenizer = RegexTokenizer()
		val actual = strings.map { s: String -> regexTokenizer.tokenize(s) }

		assertEquals(expected, actual)
	}

	@Test
	fun `Operators and brackets work`() {
		val strings = listOf("|*)(", "a|b", "a*", "(())")
		val expected =
			listOf(
				listOf(Union, Star, ClosingBracket, OpeningBracket),
				listOf(Atom('a'), Union, Atom('b')),
				listOf(Atom('a'), Star),
				listOf(OpeningBracket, OpeningBracket, ClosingBracket, ClosingBracket),
			)

		val regexTokenizer = RegexTokenizer()
		val actual = strings.map { s: String -> regexTokenizer.tokenize(s) }

		assertEquals(expected, actual)
	}

	@Test
	fun `Every group is a group work`() {
		val input = """\a\A\l\L\u\U\s\S\d\D\i\I."""

		val regexTokenizer = RegexTokenizer()
		val actual = regexTokenizer.tokenize(input)

		assertEquals((input.length - 1) / 2 + 1, actual.size)
		for (token in actual) {
			assert(token is Group)
		}
	}

	@Test
	fun `Digits group is a right group`() {
		val input = "\\d"
		val expected = listOf(Group("0123456789".toSet()))

		val regexTokenizer = RegexTokenizer()
		val actual = regexTokenizer.tokenize(input)

		assertEquals(expected, actual)
	}

	@Test
	fun `Some characters escape fine`() {
		val input = """\|\*\.\\\(\)"""
		val expected = "|*.\\()".map { c: Char -> Atom(c) }

		val regexTokenizer = RegexTokenizer()
		val actual = regexTokenizer.tokenize(input)

		assertEquals(expected, actual)
	}

	@Test
	fun `Wrong characters do not escape`() {
		val badInput = "some big string with \\b"

		val regexTokenizer = RegexTokenizer()
		assertThrows<BadRegexFormatException> {
			regexTokenizer.tokenize(badInput)
		}
	}

	@Test
	fun `Last symbol cannot be backslash`() {
		val badInput = "some big string with \\"

		val regexTokenizer = RegexTokenizer()
		assertThrows<BadRegexFormatException> {
			regexTokenizer.tokenize(badInput)
		}
	}

	@Test
	fun `No strange symbols`() {
		val badInput = "❤️"

		val regexTokenizer = RegexTokenizer()
		assertThrows<BadRegexFormatException> {
			regexTokenizer.tokenize(badInput)
		}
	}
}
