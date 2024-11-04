@lexer @valid @noErrors
Feature: Lexer without errors
	Verify lexer does not return any errors on valid source code examples.

	@blocks
	Scenario Outline: Valid blocks do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                    |
			| valid/blocks/blockInBlock.exe |
			| valid/blocks/limitScope.exe   |
			| valid/blocks/max.exe          |

	@comments
	Scenario Outline: Valid comments do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                              |
			| valid/comments/mixingComments.exe      |
			| valid/comments/multiLineComments.exe  |
			| valid/comments/singleLineComments.exe |

	@conditionals
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

	@functions
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

	@identifiers
	Scenario Outline: Valid identifiers do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                      |
			| valid/identifiers/functions.exe |
			| valid/identifiers/variables.exe |

	@loops
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

	@separators
	Scenario Outline: Valid separators do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                              |
			| valid/separator/delimitedStatements.exe |
			| valid/separator/instructionBlock.exe    |
			| valid/separator/valueOfFunctions.exe    |

	@variables
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

	@various
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
