package org.exeval.lexer.regexparser

import org.exeval.automata.interfaces.Regex
import org.exeval.lexer.interfaces.RegexParser

class RegexParserImpl(
	private val regexTokenizer: RegexTokenizer,
	private val tokenChecker: TokenChecker,
	private val tokensToRegex: TokensToRegex,
) : RegexParser {
	constructor() : this(RegexTokenizer(), TokenChecker(), TokensToRegex())

	override fun parse(pattern: String): Regex {
		val tokens = regexTokenizer.tokenize(pattern)
		tokenChecker.check(tokens)
		val regex = tokensToRegex.convert(tokens)

		return regex
	}
}
