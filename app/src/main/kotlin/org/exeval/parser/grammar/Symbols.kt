package org.exeval.parser.grammar

import kotlinx.serialization.Serializable
import org.exeval.utilities.TokenCategories as Token

@Serializable
object ValueSymbol: TerminalGroup {
	override fun values() = listOf(
		Token.LiteralInteger,
		Token.LiteralBoolean,
		Token.LiteralNope,
		Token.LiteralNothing,
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
		listOf(
			StructAccessSymbol,
			Token.OperatorAssign,
			ExpressionSymbol,
		),
		listOf(
			HereAccess,
			Token.OperatorAssign,
			ExpressionSymbol,
		)
	)
}

@Serializable
object ConstructorDeclarationParamsSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordCtor,
			Token.PunctuationLeftRoundBracket,
			FunctionParamsSymbol,
			Token.PunctuationRightRoundBracket,
			Token.OperatorAssign,
		),
		listOf(
			Token.KeywordCtor,
			Token.LiteralNope,
			Token.OperatorAssign,
		),
	)
}

@Serializable
object ConstructorDeclarationSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			ConstructorDeclarationParamsSymbol,
			SimpleExpressionSymbol,
		),
		listOf(
			ConstructorDeclarationParamsSymbol,
			ExpressionBlockSymbol,
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

TODO("oopsie")
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
			StructAccessSymbol,
			Operator2ArgSymbol,
			ExpressionSymbol,
		),
		listOf(
			HereAccess,
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
		listOf(StructAccessSymbol),
		listOf(HereAccess),
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
			TopLevelStatementsDeclarationsSymbol,
		),
	)
}

@Serializable
object HereAccess: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordHere,
		),
		listOf(
			Token.KeywordHere,
			Token.PunctuationDot,
			StructAccessSymbol
		),
		listOf(
			Token.KeywordHere,
			Token.PunctuationDot,
			Token.IdentifierNontype,
		),
		listOf(
			Token.KeywordHere,
			Token.PunctuationDot,
			ArrayAccessSymbol
		),
	)
}

@Serializable
object StructAccessSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			StructAccessByArraySymbol
		),
		listOf(
			StructAccessByFunctionCallSymbol
		),
		listOf(
			StructAccessByIdentyfierNonTypeSymbol
		),
	)
}

@Serializable
object StructAccessByArraySymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			ArrayAccessSymbol,
			Token.PunctuationDot,
			Token.IdentifierNontype,
		),
		listOf(
			ArrayAccessSymbol,
			Token.PunctuationDot,
			ArrayAccessSymbol,
		),
		listOf(
			ArrayAccessSymbol,
			Token.PunctuationDot,
			StructAccessSymbol,
		),
	)
}

@Serializable
object StructAccessByIdentyfierNonTypeSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationDot,
			Token.IdentifierNontype,
		),
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationDot,
			ArrayAccessSymbol,
		),
		listOf(
			Token.IdentifierNontype,
			Token.PunctuationDot,
			StructAccessSymbol,
		),
	)
}

@Serializable
object StructAccessByFunctionCallSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			FunctionCallSymbol,
			Token.PunctuationDot,
			StructAccessSymbol,
		),
		listOf(
			FunctionCallSymbol,
			Token.PunctuationDot,
			Token.IdentifierNontype,
		),
		listOf(
			FunctionCallSymbol,
			Token.PunctuationDot,
			ArrayAccessSymbol,
		),
	)
}

@Serializable
object StructDefinitionSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			Token.KeywordUct,
			Token.IdentifierType,
			Token.OperatorAssign,
			Token.PunctuationLeftCurlyBracket,
			StructDefinitionBodySymbol,
			Token.PunctuationRightCurlyBracket,
		),
	)
}

@Serializable
object StructDefinitionBodySymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			StructDefinitionBodyPropertySymbol,
		),
		listOf(
			StructDefinitionBodyPropertySymbol,
			Token.PunctuationSemicolon,
			StructDefinitionBodySymbol,
		),
		listOf(
			StructDefinitionBodyPropertySymbol,
			StructDefinitionBodySymbol,
		),
	)
}

@Serializable
object StructDefinitionBodyPropertySymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			VariableDeclarationSymbol,
		),
		listOf(
			ConstantDeclarationSymbol,
		),
		listOf(
			Token.KeywordLet,
			Token.IdentifierNontype,
			Token.PunctuationColon,
			TypeSymbol,
		),
		listOf(
			ConstructorDeclarationSymbol,
		),
	)
}

@Serializable
object TopLevelStatementsDeclarationsSymbol: GrammarSymbol {
	override fun productions() = listOf(
		listOf(
			SimpleFunctionDefinitionSymbol,
			Token.PunctuationSemicolon,
		),
		listOf(BlockFunctionDefinitionSymbol),
		listOf(
			SimpleFunctionDefinitionSymbol,
			Token.PunctuationSemicolon,
			TopLevelStatementsDeclarationsSymbol,
		),
		listOf(
			BlockFunctionDefinitionSymbol,
			TopLevelStatementsDeclarationsSymbol,
		),
		listOf(
			ForeignFunctionDeclarationSymbol,
			TopLevelStatementsDeclarationsSymbol,
		),
		listOf(
			StructDefinitionBodyPropertySymbol,
			TopLevelStatementsDeclarationsSymbol,
		),
		listOf(
			StructDefinitionBodyPropertySymbol,
			Token.PunctuationSemicolon,
			TopLevelStatementsDeclarationsSymbol,
		),
		listOf(
			StructDefinitionBodyPropertySymbol,
		),
		listOf(
			StructDefinitionBodyPropertySymbol,
			Token.PunctuationSemicolon,
		),
	)
}