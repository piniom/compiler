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
		),
		/*
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
			Token.OperatorAssign,
			Expression,
		),
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			Token.IdentifierType,
		),
		*/
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
		Token.OperatorAssign,
	)
}

object Operator1Arg: TerminalGroup {
	override fun values() = listOf(
		Token.OperatorMinus,
		Token.OperatorNot,
	)
}

object IfThenElse: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordIf,
			Expression,
			Token.KeywordThen,
			Expression,
		),
		listOf(
			Token.KeywordIf,
			Expression,
			Token.KeywordThen,
			Expression,
			Token.KeywordElse,
			Expression,
		),
	)
}

object Loop: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLoop,
			Expression,
		),
		listOf(
			Token.KeywordLoop,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
			Expression,
		),
	)
}

object Break: GrammarSymbol {
	override fun productions() = listOf(
		listOf(Token.KeywordBreak),
		listOf(
			Token.KeywordBreak,
			Expression,
		),
		listOf(
			Token.KeywordBreak,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
		),
		listOf(
			Token.KeywordBreak,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
			Expression,
		),
	)
}

object Expression: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.PunctuationLeftCurlyBracket,
			Expression,
			Token.PunctuationRightCurlyBracket,
		),
		listOf(ExpressionElement),
		listOf(
			ExpressionElement,
			Token.PunctuationSemicolon,
			Expression,
		),
		/*
		listOf(
			ExpressionElement,
			Operator2Arg,
			Expression,
		),
		listOf(
			Operator1Arg,
			Expression,
		),
		*/
	)

	object ExpressionElement: GrammarSymbol {
		override fun productions() = listOf(
			listOf(Value),
			listOf(VariableDeclaration),
			/*
			listOf(FunctionDeclaration),
			listOf(FunctionCall),
			listOf(IfThenElse),
			listOf(Loop),
			listOf(Break),
			*/
		)
	}
}

object ErrorSymbol: Terminal

object Program: GrammarSymbol {
	override fun productions() = listOf(
		listOf(FunctionsDeclarations),
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
