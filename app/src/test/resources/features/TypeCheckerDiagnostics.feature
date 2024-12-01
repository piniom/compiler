@typeChecker @invalid @diagnostics
Feature: Type Checker diagnostics
  Verify type checker return correct diagnostics on invalid source code examples.

  @blocks
  Scenario Outline: Invalid blocks do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                    | message                                        | line | column | endLine | endColumn |
      | invalid/blocks/wrong_type.exe | "Assignment type does not match variable type" | 4    | 18     | 6       | 7         |

  @conditionals
  Scenario Outline: Invalid conditionals do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                               | message                                          | line | column | endLine | endColumn |
      | invalid/conditionals/assigningInCondition.exe                  | "Condition expression must be Bool"              | 3    | 8      | 3       | 13        |
      | invalid/conditionals/conditionNotBoolType.exe                  | "Condition expression must be Bool"              | 2    | 8      | 2       | 9         |
      | invalid/conditionals/mismachedTypesUsingNestedConditionals.exe | "Then and else branches must have the same type" | 2    | 5      | 6       | 6         |
      | invalid/conditionals/thenAndElseEvaluateToDifferentTypes.exe   | "Then and else branches must have the same type" | 2    | 5      | 2       | 27        |
      | invalid/conditionals/usingInvalidFunctionCallAsCondition.exe   | "Condition expression must be Bool"              | 4    | 8      | 4       | 18         |

    #TODO: there is no check if "if" without "else" is returning Nope

  @conditionals
  Scenario: Invalid conditionals using various features do cause type checker errors
    Given ExEval source code file "invalid/conditionals/mismachedTypesInNestedConditionals.exe"
    When source code is passed through type checker
    Then returns diagnostics:
      | message                                        | line | column | endLine | endColumn |
      | Then and else branches must have the same type | 3    | 9      | 3       | 32        |
      | Then and else branches must have the same type | 2    | 5      | 6       | 6         |

  @functions
  Scenario Outline: Invalid functions do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                   | message                                                    | line | column | endLine | endColumn |
      | invalid/functions/incompatibleReturnType.exe | "Function return type does not match declared return type" | 4    | 21     | 6       | 2         |

    #TODO: InconsistientBreakLoopType.exe - there is no check for that

  @separator
  Scenario Outline: Invalid separator do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                | message                                                    | line | column | endLine | endColumn |
      | invalid/separator/conditional.exe         | "Then and else branches must have the same type"           | 3    | 5      | 7       | 15        |
      | invalid/separator/invalidBlock.exe        | "Function return type does not match declared return type" | 0    | 21     | 8       | 2         |
      | invalid/separator/variableDeclaration.exe | "Function return type does not match declared return type" | 0    | 21     | 3       | 2         |

    #TODO: no tests for:
    # -  Initializer type does not match declared type
    # -  Assignment type does not match variable type
    # -  argument types
    # -  bad operands

  @various
  Scenario Outline: Invalid various do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                            | message                                          | line | column | endLine | endColumn |
      | invalid/various/KnownReturnedType.exe | "Then and else branches must have the same type" | 3    | 5      | 8       | 6         |