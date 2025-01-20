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
						throw BadRegexFormatException(TOO_MUCH_BRACKETS_ERROR.format(i))
					}
					openingStack.pop()
				}

				else -> {}
			}
		}
		if (openingStack.isNotEmpty()) {
			throw BadRegexFormatException(NOT_CLOSED_BRACKET_ERROR.format(openingStack.peek()))
		}
	}

	private fun checkOperators(tokens: List<RegexToken>) {
		if (tokens.first() in setOf(RegexToken.Union, RegexToken.Star)) {
			throw BadRegexFormatException(FIRST_SYMBOL_ERROR)
		}
		if (tokens.last() == RegexToken.Union) {
			throw BadRegexFormatException(LAST_SYMBOL_ERROR)
		}
		for (i in 1..<(tokens.size - 1)) {
			val prev = tokens[i - 1]
			val cur = tokens[i]
			val next = tokens[i + 1]
			if (cur == RegexToken.Union) {
				if (prev in
					setOf(
						RegexToken.Union,
						RegexToken.OpeningBracket,
					)
				) {
					throw BadRegexFormatException(ILLEGAL_OPERATORS_ERROR.format(i - 1))
				}
				if (next in
					setOf(
						RegexToken.Union,
						RegexToken.Star,
						RegexToken.ClosingBracket,
					)
				) {
					throw BadRegexFormatException(ILLEGAL_OPERATORS_ERROR.format(i))
				}
			}
		}
	}
}

private const val TOO_MUCH_BRACKETS_ERROR = "Too much brackets at index %d"
private const val NOT_CLOSED_BRACKET_ERROR = "Bracket at %d was never closed"
private const val FIRST_SYMBOL_ERROR = "First symbol is Union or Star"
private const val LAST_SYMBOL_ERROR = "Last symbol is Union"
private const val ILLEGAL_OPERATORS_ERROR = "There is illegal combination of operators starting at %d"
