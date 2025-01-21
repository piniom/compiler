package org.exeval.parser.grammar

import kotlinx.serialization.Serializable
import org.exeval.utilities.TokenCategories as Token

@Serializable
object ValueSymbol: TerminalGroup {
	override fun values() = listOf(
		Token.LiteralInteger,
		Token.LiteralBoolean,
		Token.LiteralNope,
	)
}

@Serializable
object VariableReferenceSymbol : TerminalGroup {
	override fun values() = listOf(
		Token.IdentifierNontype,
	)
}

@Serializable
object TypeSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.PunctuationLeftSquareBracket,
			TypeSymbol,
			Token.PunctuationRightSquareBracket,
		),
		listOf(
			Token.IdentifierType
		),
	)
}

@Serializable
object VariableDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			TypeSymbol,
		),
		listOf(
			Token.KeywordLet,
			Token.KeywordMut,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			TypeSymbol,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
	)
}

@Serializable
object ConstantDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordLet,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			TypeSymbol,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
	)
}

@Serializable
object VariableAssignmentSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
		listOf(
			ArrayAccessSymbol,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
	)
}

@Serializable
object FunctionDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			FunctionParamsSymbol,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			TypeSymbol,
			Token.OperatorAssign,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierEntrypoint,
			Token.LiteralNope,
			Token.PunctuationArrow,
			TypeSymbol,
			Token.OperatorAssign,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierEntrypoint,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			TypeSymbol,
			Token.OperatorAssign,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.LiteralNope,
			Token.PunctuationArrow,
			TypeSymbol,
			Token.OperatorAssign,
		),
		listOf(
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			TypeSymbol,
			Token.OperatorAssign,
		),
	)
}

@Serializable
object SimpleFunctionDefinitionSymbol : GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			FunctionDeclarationSymbol,
			SimpleExpressionSymbol,
		),
	)
}

@Serializable
object BlockFunctionDefinitionSymbol : GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			FunctionDeclarationSymbol,
			ExpressionBlockSymbol,
		),
	)
}

@Serializable
object ForeignFunctionDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordForeign,
			Token.KeywordFoo,
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			FunctionParamsSymbol,
			Token.PunctuationRightRoundBracket,
			Token.PunctuationArrow,
			TypeSymbol,
		),
	)
}

@Serializable
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

@Serializable
object FunctionParamSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationColon,
			TypeSymbol,
		),
	)
}

@Serializable
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
			Token.LiteralNope,
		),
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationLeftRoundBracket,
			Token.PunctuationRightRoundBracket,
		),
	)
}

@Serializable
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

@Serializable
object AllocationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordNew,
			TypeSymbol,
			Token.PunctuationLeftRoundBracket,
			FunctionCallArgumentsSymbol,
			Token.PunctuationRightRoundBracket,
		),
	)
}

@Serializable
object DeallocationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordDel,
			ExpressionSymbol,
		),
	)
}

@Serializable
object ArrayIndexSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.PunctuationLeftSquareBracket,
			ExpressionSymbol,
			Token.PunctuationRightSquareBracket,
		),
		listOf(
			Token.PunctuationLeftSquareBracket,
			ExpressionSymbol,
			Token.PunctuationRightSquareBracket,
			ArrayIndexSymbol,
		),
	)
}

@Serializable
object ArrayAccessSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			ArrayIndexSymbol
		),
		listOf(
			FunctionCallSymbol,
			ArrayIndexSymbol
		),
		listOf(
			Token.PunctuationLeftRoundBracket,
			ExpressionSymbol,
			Token.PunctuationRightRoundBracket,
			ArrayIndexSymbol
		)
	)
}

@Serializable
object IfSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordIf,
			ExpressionSymbol,
			Token.KeywordThen,
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

@Serializable
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

@Serializable
object BreakSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordBreak,
		),
		listOf(
			Token.KeywordBreak,
			Token.PunctuationMonkey,
			Token.IdentifierNontype,
		),
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

@Serializable
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

@Serializable
object Operator1ArgSymbol: TerminalGroup {
	override fun values() = listOf(
		Token.OperatorMinus,
		Token.OperatorNot,
	)
}

@Serializable
object ArithmeticExpressionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			ValueSymbol,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			VariableReferenceSymbol,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			FunctionCallSymbol,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			ArrayAccessSymbol,
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
		listOf(
			Token.PunctuationLeftRoundBracket,
			ExpressionSymbol,
			Token.PunctuationRightRoundBracket,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
	)
}

@Serializable
object SimpleExpressionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(ValueSymbol),
		listOf(VariableReferenceSymbol),
		listOf(ArithmeticExpressionSymbol),
		listOf(VariableDeclarationSymbol),
		listOf(ConstantDeclarationSymbol),
		listOf(VariableAssignmentSymbol),
		listOf(SimpleFunctionDefinitionSymbol),
		listOf(FunctionCallSymbol),
		listOf(IfSymbol),
		listOf(LoopSymbol),
		listOf(BreakSymbol),
		listOf(AllocationSymbol),
		listOf(DeallocationSymbol),
		listOf(ArrayAccessSymbol),
	)
}

@Serializable
object ExpressionBlockSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.PunctuationLeftCurlyBracket,
			ExpressionChainSymbol,
			Token.PunctuationRightCurlyBracket,
		),
	)

	@Serializable
	object ExpressionChainSymbol: GrammarSymbol {
		override fun productions() = listOf(
			listOf(SimpleExpressionSymbol),
			listOf(ExpressionBlockSymbol),
			listOf(
				SimpleExpressionSymbol,
				Token.PunctuationSemicolon,
				ExpressionChainSymbol,
			),
			listOf(
				ExpressionBlockSymbol,
				Token.PunctuationSemicolon,
				ExpressionChainSymbol,
			),
			listOf(
				BlockFunctionDefinitionSymbol,
				ExpressionChainSymbol,
			),
			listOf(
				LoopSymbol,
				ExpressionChainSymbol,
			),
			listOf(
				IfSymbol,
				ExpressionChainSymbol,
			),
		)
	}
}

@Serializable
object ExpressionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(SimpleExpressionSymbol),
		listOf(ExpressionBlockSymbol),
	)
}

@Serializable
object EndOfProgramSymbol: Terminal

@Serializable
object ProgramSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			FunctionsDeclarationsSymbol,
		),
	)
}

@Serializable
object FunctionsDeclarationsSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			SimpleFunctionDefinitionSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(BlockFunctionDefinitionSymbol),
		listOf(
			SimpleFunctionDefinitionSymbol,
			Token.PunctuationSemicolon,
			FunctionsDeclarationsSymbol,
		),
		listOf(
			BlockFunctionDefinitionSymbol,
			FunctionsDeclarationsSymbol,
		),
		listOf(
			ForeignFunctionDeclarationSymbol,
			FunctionsDeclarationsSymbol
		),
	)
}