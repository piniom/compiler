package org.exeval.parser.grammar

import org.exeval.utilities.TokenCategories as Token

object Value: TerminalGroup {
	override fun values() = listOf(
		Token.LiteralInteger,
		Token.LiteralBoolean,
		Token.LiteralNope,
	)
}

object VariableDeclaration: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLet,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
			Token.OperatorAssign,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
			Token.OperatorAssign,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
	)
}

object VariableAssignment: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.OperatorAssign,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		)
	)
}

object FunctionDeclaration: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			FunctionDeclarationArguments,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			Token.IdentifierType,
			Token.OperatorAssign,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierEntrypoint,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			Token.IdentifierType,
			Token.OperatorAssign,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			Token.IdentifierType,
			Token.OperatorAssign,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
	)
}

object FunctionDeclarationArguments: GrammarSymbol {
	override fun productions() = listOf(
		listOf(FunctionArg),
		listOf(
			FunctionArg,
			Token.PunctuationComma,
			FunctionDeclarationArguments,
		),
	)

	object FunctionArg: GrammarSymbol {
		override fun productions() = listOf(
			listOf(
				Token.IdentifierNontype,
				Token.PunctuationColon,
				Token.IdentifierType,
			),
		)
	}
}

object FunctionCall: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			FunctionCallArguments,
			Token.PunctuationRightRoundBracket,
		),
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
		),
	)
}

object FunctionCallArguments: GrammarSymbol {
	override fun productions() = listOf(
		listOf(FunctionArg),
		listOf(
			FunctionArg,
			Token.PunctuationComma,
			FunctionCallArguments,
		),
	)

	object FunctionArg: GrammarSymbol {
		override fun productions() = listOf(
			listOf(Expression),
			listOf(
				Token.IdentifierNontype,
				Token.OperatorAssign,
				Expression,
			),
		)
	}
}

object IfThenElse: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordIf,
			Expression,
			Token.KeywordThen,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordIf,
			Expression,
			Token.KeywordThen,
			Expression,
			Token.KeywordElse,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
	)
}

object Loop: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLoop,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordLoop,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
	)
}

object Break: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordBreak,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(
			Token.KeywordBreak,
			Expression,
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
		listOf(
			Token.KeywordBreak,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
			Expression,
			ErrorSymbol,
			Token.PunctuationSemicolon,
		),
	)
}

object Operator2Arg: TerminalGroup {
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

object Operator1Arg: TerminalGroup {
	override fun values() = listOf(
		Token.OperatorMinus,
		Token.OperatorNot,
	)
}

object ArithmeticExpression: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Value,
			Operator2Arg,
			Expression,
		),
		listOf(
			Token.IdentifierNontype,
			Operator2Arg,
			Expression,
		),
		listOf(
			FunctionCall,
			Operator2Arg,
			Expression,
		),
		listOf(
			Operator1Arg,
			Expression,
		),
		listOf(
			Token.PunctuationLeftRoundBracket,
			Expression,
			Token.PunctuationRightRoundBracket,
		),
	)
}

object SimpleExpression: GrammarSymbol {
	override fun productions() = listOf(
		listOf(Value),
		listOf(Token.IdentifierNontype),
		listOf(ArithmeticExpression),
		listOf(FunctionCall),
	)
}

// These always end with ;
object ComplexExpression: GrammarSymbol {
	override fun productions() = listOf(
		listOf(VariableDeclaration),
		listOf(VariableAssignment),
		listOf(FunctionDeclaration),
		listOf(IfThenElse),
		listOf(Loop),
		listOf(Break),
	)
}

object ExpressionBlock: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.PunctuationLeftCurlyBracket,
			ExpressionChain,
			Token.PunctuationRightCurlyBracket,
		),
	)

	object ExpressionChain: GrammarSymbol {
		override fun productions() = listOf(
			listOf(SimpleExpression),
			listOf(ComplexExpression),
			listOf(ExpressionBlock),
			listOf(
				SimpleExpression,
				ErrorSymbol,
				Token.PunctuationSemicolon,
				ExpressionChain,
			),
			listOf(
				ComplexExpression,
				ExpressionChain,
			),
			listOf(
				ExpressionBlock,
				ErrorSymbol,
				Token.PunctuationSemicolon,
				ExpressionChain,
			),
		)
	}
}

object Expression: GrammarSymbol {
	override fun productions() = listOf(
		listOf(SimpleExpression),
		listOf(ComplexExpression),
		listOf(ExpressionBlock),
	)
}

object ErrorSymbol: Terminal

object EndOfProgramSymbol: Terminal

object Program: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			FunctionsDeclarations,
			EndOfProgramSymbol,
		),
	)

	object FunctionsDeclarations: GrammarSymbol {
		override fun productions() = listOf(
			listOf(FunctionDeclaration),
			listOf(
				FunctionDeclaration,
				FunctionsDeclarations,
			),
		)
	}
}
