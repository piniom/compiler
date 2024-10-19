@lexer @valid
Feature: Lexer without errors
	Verify lexer does not return any errors on valid source code examples.

	@blocks @notImplemented
	Scenario Outline: Valid blocks do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                    |
			| valid/blocks/blockinblock.exe |
			| valid/blocks/limitscope.exe   |
			| valid/blocks/max.exe          |

	@comments @notImplemented
	Scenario Outline: Valid comments do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                              |
			| valid/comments/mixing_comments.exe      |
			| valid/comments/multi_line_comments.exe  |
			| valid/comments/single_line_comments.exe |

	@conditionals @notImplemented
	Scenario Outline: Valid conditionals do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                                      |
			| valid/conditionals/conditionalsInsideLoop.exe   |
			| valid/conditionals/conditionalAndOrNot.exe      |
			| valid/conditionals/conditionalCallsFunction.exe |
			| valid/conditionals/ifWithoutElse.exe            |
			| valid/conditionals/ifWithElse.exe               |
			| valid/conditionals/nestedConditionals.exe       |

	@functions @notImplemented
	Scenario Outline: Valid functions do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                                   |
			| valid/foonctions/constantFunction.exe        |
			| valid/foonctions/functionWithArguments.exe   |
			| valid/foonctions/multipleNestedFunctions.exe |
			| valid/foonctions/nestedFunction.exe          |
			| valid/foonctions/recursiveFunction.exe       |

	@identifiers @notImplemented
	Scenario Outline: Valid identifiers do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                      |
			| valid/identifiers/functions.exe |
			| valid/identifiers/variables.exe |

	@loops @notImplemented
	Scenario Outline: Valid loops do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                                          |
			| valid/loops/BreakWithLoopValueInBreak.exe           |
			| valid/loops/LoopsIterateProperly.exe                |
			| valid/loops/LoopWithValueInBracketsInBreak.exe      |
			| valid/loops/MultipleBreaksWithTheSameType.exe       |
			| valid/loops/NestedLoopsBreaksProperlyWithLabels.exe |

	@separators @notImplemented
	Scenario Outline: Valid separators do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                              |
			| valid/separator/delimitedStatements.exe |
			| valid/separator/instructionBlock.exe    |
			| valid/separator/valueOfFunctions.exe    |

	@variables @notImplemented
	Scenario Outline: Valid variables do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                                       |
			| valid/variables/BasicOperationsOverVariables.exe |
			| valid/variables/ChangingMutableVariable.exe      |
			| valid/variables/CreatingSomeVariables.exe        |
			| valid/variables/InitializingVariables.exe        |
			| valid/variables/ReassigningVariables.exe         |

	@various @notImplemented
	Scenario Outline: Valid programs using various features do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                        |
			| valid/various/Arithmetic.exe      |
			| valid/various/KnownReturnType.exe |
			| valid/various/Nested.exe          |
			| valid/various/Parantheses.exe     |
			| valid/various/Whitespace.exe      |
