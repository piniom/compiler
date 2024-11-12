package org.exeval.parser.grammar

import org.exeval.utilities.TokenCategories as Token

object ValueSymbol: TerminalGroup {
	override fun values() = listOf(
		Token.LiteralInteger,
		Token.LiteralBoolean,
		Token.LiteralNope,
	)
}

object VariableDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
		),
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
	)
}

object ConstantDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLet,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
	)
}

object VariableAssignmentSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.OperatorAssign,
			ExpressionSymbol,
		)
	)
}

object FunctionDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			FunctionParamsSymbol,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			Token.IdentifierType,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierEntrypoint,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			Token.IdentifierType,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			Token.IdentifierType,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
	)
}

object FunctionParamsSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(FunctionParamSymbol),
		listOf(
			FunctionParamSymbol,
			Token.PunctuationComma,
			FunctionParamsSymbol,
		),
	)
}

object FunctionParamSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
		),
	)
}

object FunctionCallSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			FunctionCallArgumentsSymbol,
			Token.PunctuationRightRoundBracket,
		),
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
		),
	)
}

object FunctionCallArgumentsSymbol: GrammarSymbol {
	override fun productions() = listOf(
		// Calling with named arguments is indistinguishable
		// from variable assignment from grammar's point
		// of view. Assignment is also part of expression.
		listOf(ExpressionSymbol),
		listOf(
			ExpressionSymbol,
			Token.PunctuationComma,
			FunctionCallArgumentsSymbol,
		),
	)
}

object IfThenSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordIf,
			ExpressionSymbol,
			Token.KeywordThen,
			ExpressionSymbol,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
	)
}

object IfThenWithoutSemicolonSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordIf,
			ExpressionSymbol,
			Token.KeywordThen,
			ExpressionSymbol,
		),
	)
}

object IfThenElseSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordIf,
			ExpressionSymbol,
			Token.KeywordThen,
			ExpressionSymbol,
			Token.KeywordElse,
			ExpressionSymbol,
		),
		listOf(
			Token.KeywordIf,
			ExpressionSymbol,
			Token.KeywordThen,
			ExpressionSymbol,
			Token.KeywordElse,
			ExpressionSymbol,
		),
	)
}

object LoopSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLoop,
			ExpressionSymbol,
		),
		listOf(
			Token.KeywordLoop,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
			ExpressionSymbol,
		),
	)
}

object BreakKeywordSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordBreak,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordBreak,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
	)
}

object BreakKeywordWithoutSemicolonSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(Token.KeywordBreak),
		listOf(
			Token.KeywordBreak,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
		),
	)
}

object BreakExpressionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordBreak,
			ExpressionSymbol,
		),
		listOf(
			Token.KeywordBreak,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
			ExpressionSymbol,
		),
	)
}

object Operator2ArgSymbol: TerminalGroup {
	override fun values() = listOf(
		Token.OperatorPlus,
		Token.OperatorMinus,
		Token.OperatorStar,
		Token.OperatorDivision,
		Token.OperatorOr,
		Token.OperatorAnd,
		Token.OperatorGreater,
		Token.OperatorLesser,
		Token.OperatorGreaterEqual,
		Token.OperatorLesserEqual,
		Token.OperatorEqual,
		Token.OperatorNotEqual,
	)
}

object Operator1ArgSymbol: TerminalGroup {
	override fun values() = listOf(
		Token.OperatorMinus,
		Token.OperatorNot,
	)
}

object ArithmeticExpressionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			ValueSymbol,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			Token.IdentifierNontype,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			FunctionCallSymbol,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			Operator1ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			Token.PunctuationLeftRoundBracket,
			ExpressionSymbol,
			Token.PunctuationRightRoundBracket,
		),
	)
}

object SimpleExpressionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(ValueSymbol),
		listOf(Token.IdentifierNontype),
		listOf(ArithmeticExpressionSymbol),
		listOf(VariableDeclarationSymbol),
		listOf(ConstantDeclarationSymbol),
		listOf(VariableAssignmentSymbol),
		listOf(FunctionDeclarationSymbol),
		listOf(FunctionCallSymbol),
		listOf(IfThenElseSymbol),
		listOf(LoopSymbol),
		listOf(BreakExpressionSymbol),
	)
}

object ExpressionWithSemicolonSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(IfThenSymbol),
		listOf(BreakKeywordSymbol),
	)
}

object LastExpressionInBlockSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(BreakKeywordWithoutSemicolonSymbol),
		listOf(IfThenWithoutSemicolonSymbol),
	)
}

object ExpressionBlockSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.PunctuationLeftCurlyBracket,
			ExpressionChainSymbol,
			Token.PunctuationRightCurlyBracket,
		),
		listOf(
			Token.PunctuationLeftCurlyBracket,
			LastExpressionInBlockSymbol,
			Token.PunctuationRightCurlyBracket,
		),
		listOf(
			Token.PunctuationLeftCurlyBracket,
			ExpressionChainSymbol,
			Token.PunctuationSemicolon,
			LastExpressionInBlockSymbol,
			Token.PunctuationRightCurlyBracket,
		),
	)

	object ExpressionChainSymbol: GrammarSymbol {
		override fun productions() = listOf(
			listOf(SimpleExpressionSymbol),
			listOf(ExpressionWithSemicolonSymbol),
			listOf(ExpressionBlockSymbol),
			listOf(
				SimpleExpressionSymbol,
				ErrorSymbol,
				Token.PunctuationSemicolon,
				ExpressionChainSymbol,
			),
			listOf(
				ExpressionWithSemicolonSymbol,
				ExpressionChainSymbol,
			),
			listOf(
				ExpressionBlockSymbol,
				ErrorSymbol,
				Token.PunctuationSemicolon,
				ExpressionChainSymbol,
			),
		)
	}
}

object ExpressionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(SimpleExpressionSymbol),
		listOf(ExpressionWithSemicolonSymbol),
		listOf(ExpressionBlockSymbol),
	)
}

object ErrorSymbol: Terminal

object EndOfProgramSymbol: Terminal

object ProgramSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			FunctionsDeclarationsSymbol,
			EndOfProgramSymbol,
		),
	)
}

object FunctionsDeclarationsSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(FunctionDeclarationSymbol),
		listOf(
			FunctionDeclarationSymbol,
			FunctionsDeclarationsSymbol,
		),
	)
}
