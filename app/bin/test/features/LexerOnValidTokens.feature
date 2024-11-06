@lexer @valid @tokens
Feature: Lexer tokens
	Verify tokens returned by lexer on valid source code examples.

	@identifiers @needsFix
	Scenario: Verify tokens returned on valid variable identifiers
		Given ExEval source code file "valid/identifiers/variables.exe"
		When source code is passed through lexer
		Then returned token list matches
			| text | categories                              |
			| foo  | KeywordFoo, IdentifierNontype           |
			| main | IdentifierNontype, IdentifierEntrypoint |
			| ()   | LiteralNope                             |
			| ->   | PunctuationArrow                        |
			| Int  | IdentifierType                          |
			| =    | OperatorAssign                          |
			| {    | PunctuationLeftCurlyBracket             |
