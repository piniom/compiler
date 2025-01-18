package org.exeval.utilities

import kotlinx.serialization.Serializable
import org.exeval.utilities.interfaces.TokenCategory
import org.exeval.parser.grammar.Terminal

@Serializable
enum class TokenCategories(val regex: String) : TokenCategory, Terminal {
	PunctuationSemicolon(";"),
	PunctuationColon(":"),
	PunctuationComma(","),
	PunctuationArrow("->"),
	PunctuationMonkey("@"),
	PunctuationLeftCurlyBracket("{"),
	PunctuationRightCurlyBracket("}"),
	PunctuationLeftSquareBracket("["),
	PunctuationRightSquareBracket("]"),
	PunctuationLeftRoundBracket("\\("),
	PunctuationRightRoundBracket("\\)"),
	LiteralInteger("(\\d)(\\d)*"),
	LiteralBoolean("(true|false)"),
	LiteralNope("\\(\\)"),
	OperatorPlus("+"),
	OperatorMinus("-"),
	OperatorStar("\\*"),
	OperatorDivision("/"),
	OperatorModulo("%"),
	OperatorOr("or"),
	OperatorAnd("and"),
	OperatorNot("not"),
	OperatorGreater(">"),
	OperatorLesser("<"),
	OperatorGreaterEqual(">="),
	OperatorLesserEqual("<="),
	OperatorEqual("=="),
	OperatorNotEqual("!="),
	OperatorAssign("="),
	KeywordIf("if"),
	KeywordThen("then"),
	KeywordElse("else"),
	KeywordLoop("loop"),
	KeywordFoo("foo"),
	KeywordForeign("foreign"),
	KeywordBreak("break"),
	KeywordReturn("return"),
	KeywordLet("let"),
	KeywordMut("mut"),
	KeywordNew("new"),
	KeywordDel("del"),
	IdentifierType("\\u(\\l|\\u)*"),
	IdentifierNontype("(\\l|_)(\\i)*"),
	IdentifierEntrypoint("main"),
	Whitespace("\\s");
}
