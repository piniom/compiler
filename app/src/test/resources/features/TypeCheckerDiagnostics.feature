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
      | invalid/blocks/wrong_type.exe | "Assignment type does not match variable type" | 4    | 17     | 6       | 6         |

  @conditionals
  Scenario Outline: Invalid conditionals do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                                     | message                                          | line | column | endLine | endColumn |
      | invalid/conditionals/assigningInCondition.exe                  | "Condition expression must be Bool"              | 3    | 6      | 3       | 11        |
      | invalid/conditionals/conditionNotBoolType.exe                  | "Condition expression must be Bool"              | 2    | 6      | 2       | 7         |
      | invalid/conditionals/mismachedTypesUsingNestedConditionals.exe | "Then and else branches must have the same type" | 2    | 3      | 6       | 4         |
      | invalid/conditionals/thenAndElseEvaluateToDifferentTypes.exe   | "Then and else branches must have the same type" | 2    | 3      | 2       | 25        |
      | invalid/conditionals/usingInvalidFunctionCallAsCondition.exe   | "Condition expression must be Bool"              | 4    | 6      | 4       | 16        |

  @conditionals
  Scenario: Invalid conditionals using various features do cause type checker errors
    Given ExEval source code file "invalid/conditionals/mismachedTypesInNestedConditionals.exe"
    When source code is passed through type checker
    Then returns diagnostics:
      | message                                        | line | column | endLine | endColumn |
      | Then and else branches must have the same type | 3    | 8      | 3       | 31        |
      | Then and else branches must have the same type | 2    | 4      | 6       | 5         |

  @functions
  Scenario Outline: Invalid functions do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                   | message                                                    | line | column | endLine | endColumn |
      | invalid/functions/incompatibleReturnType.exe | "Function return type does not match declared return type" | 4    | 20     | 6       | 1         |

  @functions
  Scenario: Invalid functions using various features do cause type checker errors
    Given ExEval source code file "invalid/functions/badArgumentType.exe"
    When source code is passed through type checker
    Then returns diagnostics:
      | message                                     | line | column | endLine | endColumn |
      | Argument type does not match parameter type | 3    | 3      | 3       | 8         |
      | Argument type does not match parameter type | 3    | 10     | 3       | 18        |

  @variables
  Scenario: Invalid variables using various features do cause type checker errors
    Given ExEval source code file "invalid/variables/badType.exe"
    When source code is passed through type checker
    Then returns diagnostics:
      | message                                       | line | column | endLine | endColumn |
      | Initializer type does not match declared type | 1    | 18     | 1       | 19        |
      | Initializer type does not match declared type | 2    | 21     | 2       | 23        |
      | Assignment type does not match variable type  | 3    | 4      | 3       | 12        |

  @various
  Scenario: Invalid functions using various features do cause type checker errors
    Given ExEval source code file "invalid/functions/badArgumentType.exe"
    When source code is passed through type checker
    Then returns diagnostics:
      | message                                              | line | column | endLine | endColumn |
      | One of operands is NopeType!                         | 4    | 4      | 4       | 13        |
      | Operands of binary operation must have the same type | 4    | 4      | 4       | 13        |
      | Operands has to be numerical                         | 5    | 4      | 5       | 12        |
      | Operand is NopeType!                                 | 6    | 4      | 6       | 8         |
      | Operator and operand must both be the same type      | 7    | 4      | 7       | 6         |


  @separator
  Scenario Outline: Invalid separator do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                | message                                                    | line | column | endLine | endColumn |
      | invalid/separator/conditional.exe         | "Then and else branches must have the same type"           | 3    | 4      | 7       | 14        |
      | invalid/separator/invalidBlock.exe        | "Function return type does not match declared return type" | 0    | 20     | 8       | 1         |
      | invalid/separator/variableDeclaration.exe | "Function return type does not match declared return type" | 0    | 20     | 3       | 1         |

  @various
  Scenario Outline: Invalid various do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                            | message                                          | line | column | endLine | endColumn |
      | invalid/various/KnownReturnedType.exe | "Then and else branches must have the same type" | 3    | 4      | 8       | 5         |