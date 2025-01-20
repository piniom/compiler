@typeChecker @invalid @diagnostics
Feature: Type Checker diagnostics
  Verify type checker return correct diagnostics on invalid source code examples.

  @blocks
  Scenario Outline: Invalid blocks do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                    | message                                         | line | column | endLine | endColumn |
      | invalid/blocks/wrong_type.exe | "Initializer type does not match declared type" | 4    | 17     | 6       | 5         |

  @conditionals
  Scenario Outline: Invalid conditionals do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                                   | message                                          | line | column | endLine | endColumn |
      | invalid/conditionals/assigningInCondition.exe                | "Condition expression must be Bool"              | 3    | 7      | 3       | 12        |
      | invalid/conditionals/conditionNotBoolType.exe                | "Condition expression must be Bool"              | 2    | 7      | 2       | 8         |
      | invalid/conditionals/thenAndElseEvaluateToDifferentTypes.exe | "Then and else branches must have the same type" | 2    | 4      | 2       | 26        |
      | invalid/conditionals/usingInvalidFunctionCallAsCondition.exe | "Condition expression must be Bool"              | 4    | 7      | 4       | 17        |
      | invalid/conditionals/mismachedTypesInNestedConditionals.exe  | "Then and else branches must have the same type" | 3    | 8      | 3       | 31        |


  @conditionals
  Scenario: Invalid conditionals using various features do cause type checker errors
    Given ExEval source code file "invalid/conditionals/mismachedTypesUsingNestedConditionals.exe"
    When source code is passed through type checker
    Then returns diagnostics:
      | message                                                  | line | column | endLine | endColumn |
      | Then and else branches must have the same type           | 2    | 4      | 6       | 5         |
      | Function return type does not match declared return type | 1    | 20     | 7       | 1         |

  @functions
  Scenario Outline: Invalid functions do cause type checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through type checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                    | message                                                    | line | column | endLine | endColumn |
      | invalid/foonctions/incompatibleReturnType.exe | "Function return type does not match declared return type" | 4    | 20     | 6       | 2         |

  @functions
  Scenario: Invalid functions using various features do cause type checker errors
    Given ExEval source code file "invalid/foonctions/incompatibleReturnType.exe"
    When source code is passed through type checker
    Then returns diagnostics:
      | message                                                  | line | column | endLine | endColumn |
      | Function return type does not match declared return type | 4    | 20     | 6       | 1         |
      | Argument type does not match parameter type              | 3    | 10     | 3       | 18        |

  @functions
  Scenario: Invalid functions using various features do cause type checker errors
    Given ExEval source code file "invalid/foonctions/badArgumentType.exe"
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