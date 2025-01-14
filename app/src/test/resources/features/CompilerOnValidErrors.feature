@Compiler @valid @noErrors
Feature: Compiler without errors
	Verify Compiler does not return any errors on valid source code examples.

	@arrays
	Scenario Outline: Valid arrays do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                                                       |
			| valid/arrays/arrayOfArray.exe                                    |
			| valid/arrays/arrayShouldNotBeDealocatedAfterReturnedFromFunc.exe |
			| valid/arrays/doubleDealocation.exe                               |
			| valid/arrays/outsideBoundariesAccess.exe                         |
			| valid/arrays/passArrToFuncSimple.exe                             |
			| valid/arrays/simpleArray.exe                                     |
			| valid/arrays/simpleArrOfArr.exe                                  |
			| valid/arrays/simpleDealocation.exe                               |
			| valid/arrays/simpleDeclaration.exe                               |


	@blocks
	Scenario Outline: Valid blocks do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                    |
			| valid/blocks/blockInBlock.exe |
			| valid/blocks/limitScope.exe   |
			| valid/blocks/max.exe          |

	@comments
	Scenario Outline: Valid comments do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                            |
			| valid/comments/mixingComments.exe     |
			| valid/comments/multiLineComments.exe  |
			| valid/comments/singleLineComments.exe |

	@conditionals
	Scenario Outline: Valid conditionals do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
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
	Scenario Outline: Valid functions do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                                   |
			| valid/foonctions/constantFunction.exe        |
			| valid/foonctions/functionWithArguments.exe   |
			| valid/foonctions/multipleNestedFunctions.exe |
			| valid/foonctions/nestedFunction.exe          |
			| valid/foonctions/recursiveFunction.exe       |

	@foreign
	Scenario Outline: Valid foreign functions do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                           |
			| valid/foreign/bool.exe               |
			| valid/foreign/many_arguments.exe     |
			| valid/foreign/multiple_functions.exe |
			| valid/foreign/no_arguments.exe	   |
			| valid/foreign/print.exe              |

	@identifiers
	Scenario Outline: Valid identifiers do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                      |
			| valid/identifiers/functions.exe |
			| valid/identifiers/variables.exe |

	@loops
	Scenario Outline: Valid loops do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                                          |
			| valid/loops/breakWithLoopValueInBreak.exe           |
			| valid/loops/loopsIterateProperly.exe                |
			| valid/loops/loopWithValueInBracketsInBreak.exe      |
			| valid/loops/multipleBreaksWithTheSameType.exe       |
			| valid/loops/nestedLoopsBreaksProperlyWithLabels.exe |

	@separators
	Scenario Outline: Valid separators do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                              |
			| valid/separator/delimitedStatements.exe |
			| valid/separator/instructionBlock.exe    |
			| valid/separator/valueOfFunctions.exe    |

	@variables
	Scenario Outline: Valid variables do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                                       |
			| valid/variables/basicOperationsOverVariables.exe |
			| valid/variables/changingMutableVariable.exe      |
			| valid/variables/creatingSomeVariables.exe        |
			| valid/variables/initializingVariables.exe        |
			| valid/variables/reassigningVariables.exe         |

	@various
	Scenario Outline: Valid programs using various features do not cause Compiler errors
		Given ExEval source code file "<sourceFile>"
		When source code is compiled to asm
		Then no errors are returned
		Examples:
			| sourceFile                        |
			| valid/various/arithmetic.exe      |
			| valid/various/knownReturnType.exe |
			| valid/various/nested.exe          |
			| valid/various/parantheses.exe     |
			| valid/various/whitespace.exe      |
