@lexer @invalid @noErrors
Feature: Lexer without errors on invalid programs
  Verify lexer does not return any errors on invalid source code examples as they are incorrect on later stages of compilation.

  @blocks
  Scenario Outline: Invalid blocks do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                         |
      | invalid/blocks/outofscope.exe      |
      | invalid/blocks/unclosed_braces.exe |
      | invalid/blocks/wrong_type.exe      |

  @comments
  Scenario Outline: Invalid comments do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                                          |
      | invalid/comments/commented_main.exe                 |
      | invalid/comments/commented_variable_declaration.exe |

  @conditionals
  Scenario Outline: Invalid conditionals do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                                                     |
      | invalid/conditionals/assigningInCondition.exe                  |
      | invalid/conditionals/conditionNotBoolType.exe                  |
      | invalid/conditionals/ifWithoutElseEvaluateToInt.exe            |
      | invalid/conditionals/incorrectVariableScopeInThenBlock.exe     |
      | invalid/conditionals/lackingConditionalExpression.exe          |
      | invalid/conditionals/lackingThen.exe                           |
      | invalid/conditionals/mismachedTypesInNestedConditionals.exe    |
      | invalid/conditionals/mismachedTypesUsingNestedConditionals.exe |
      | invalid/conditionals/thenAndElseEvaluateToDifferentTypes.exe   |
      | invalid/conditionals/uninitializedVariablesInsideIf.exe        |
      | invalid/conditionals/usingInvalidFunctionCallAsCondition.exe   |

  @functions
  Scenario Outline: Invalid functions do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                                                |
      | invalid/foonctions/badNamedArgument.exe                   |
      | invalid/foonctions/badNumberOfArguments.exe               |
      | invalid/foonctions/duplicatedNamedArgument.exe            |
      | invalid/foonctions/duplicatedNamedArgumentTooManyArgs.exe |
      | invalid/foonctions/functionNoBrackets.exe                 |
      | invalid/foonctions/incompatibleReturnType.exe             |
      | invalid/foonctions/noReturnType.exe                       |
      | invalid/foonctions/undefFunctionCall.exe                  |
      | invalid/foonctions/variableAndFunctionSameNames.exe       |

  @identifiers
  Scenario Outline: Invalid identifiers do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                        |
      | invalid/identifiers/functions.exe |
      | invalid/identifiers/variables.exe |
      | invalid/identifiers/types.exe     |

  @loops
  Scenario Outline: Invalid loops do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                                   |
      | invalid/loops/BreakLoopOutsideOfLoop.exe     |
      | invalid/loops/BreakLoopOutsideOfRange.exe    |
      | invalid/loops/InconsistientBreakLoopType.exe |
      | invalid/loops/MultipleLoopsWithSameLabel.exe |
      | invalid/loops/UnknownLoopLabel.exe           |

  @separators
  Scenario Outline: Invalid separators do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                                |
      | invalid/separator/conditional.exe         |
      | invalid/separator/empty.exe               |
      | invalid/separator/invalidBlock.exe        |
      | invalid/separator/variableDeclaration.exe |

  @variables
  Scenario Outline: Invalid variables do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                                       |
      | invalid/variables/ChangingConstant.exe           |
      | invalid/variables/NotInitializingConstant.exe    |
      | invalid/variables/NoTypeInferenceForNow.exe      |
      | invalid/variables/NoVariableKeyword.exe          |
      | invalid/variables/NoVariableName.exe             |
      | invalid/variables/RedefiningConstantVariable.exe |
      | invalid/variables/RedefiningMutableVariable.exe  |
      | invalid/variables/WrongKeywordsForVariables.exe  |

  @various
  Scenario Outline: Invalid programs using various features do not cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then no errors are returned
    Examples:
      | sourceFile                                  |
      | invalid/various/FooToLet.exe                |
      | invalid/various/KnownReturnedType.exe       |
      | invalid/various/Parantheses.exe             |
      | invalid/various/Random.exe                  |
      | invalid/various/Reserved.exe                |
      | invalid/various/ReservedLoopIdentifiers.exe |
