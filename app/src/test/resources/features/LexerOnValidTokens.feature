@lexer @valid @tokens
Feature: Lexer tokens
	Verify tokens returned by lexer on valid source code examples.

	@identifiers
	Scenario: Verify tokens returned on valid variable identifiers
		Given ExEval source code file "valid/identifiers/variables.exe"
		When source code is passed through lexer
		Then returned token list matches
			| text               | categories                              |
			| foo                | KeywordFoo, IdentifierNontype           |
			| main               | IdentifierNontype, IdentifierEntrypoint |
			| ()                 | LiteralNope                             |
			| ->                 | PunctuationArrow                        |
			| Int                | IdentifierType                          |
			| =                  | OperatorAssign                          |
			| {                  | PunctuationLeftCurlyBracket             |
			| let                | KeywordLet, IdentifierNontype           |
			| constant           | IdentifierNontype                       |
			| :                  | PunctuationColon                        |
			| Int                | IdentifierType                          |
			| =                  | OperatorAssign                          |
			| 0                  | LiteralInteger                          |
			| ;                  | PunctuationSemicolon                    |
			| let                | KeywordLet, IdentifierNontype           |
			| mut                | KeywordMut, IdentifierNontype           |
			| variable           | IdentifierNontype                       |
			| :                  | PunctuationColon                        |
			| Int                | IdentifierType                          |
			| ;                  | PunctuationSemicolon                    |
			| let                | KeywordLet, IdentifierNontype           |
			| longConstantName   | IdentifierNontype                       |
			| :                  | PunctuationColon                        |
			| Bool               | IdentifierType                          |
			| =                  | OperatorAssign                          |
			| true               | LiteralBoolean, IdentifierNontype       |
			| ;                  | PunctuationSemicolon                    |
			| let                | KeywordLet, IdentifierNontype           |
			| mut                | KeywordMut, IdentifierNontype           |
			| longVariableName   | IdentifierNontype                       |
			| :                  | PunctuationColon                        |
			| Bool               | IdentifierType                          |
			| ;                  | PunctuationSemicolon                    |
			| let                | KeywordLet, IdentifierNontype           |
			| mut                | KeywordMut, IdentifierNontype           |
			| snaked_name        | IdentifierNontype                       |
			| :                  | PunctuationColon                        |
			| Int                | IdentifierType                          |
			| ;                  | PunctuationSemicolon                    |
			| let                | KeywordLet, IdentifierNontype           |
			| mut                | KeywordMut, IdentifierNontype           |
			| numbered123        | IdentifierNontype                       |
			| :                  | PunctuationColon                        |
			| Int                | IdentifierType                          |
			| ;                  | PunctuationSemicolon                    |
			| let                | KeywordLet, IdentifierNontype           |
			| uPPERCASE_CONSTANT | IdentifierNontype                       |
			| :                  | PunctuationColon                        |
			| Int                | IdentifierType                          |
			| =                  | OperatorAssign                          |
			| 42                 | LiteralInteger                          |
			| ;                  | PunctuationSemicolon                    |
			| 0                  | LiteralInteger                          |
			| }                  | PunctuationRightCurlyBracket            |
