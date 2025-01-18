@nameResolution @invalid @diagnostics
Feature: Name Resolution diagnostics
  Verify name resolution return correct diagnostics on invalid source code examples.

  @blocks
  Scenario Outline: Invalid blocks do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                    | message                           | line | column | endLine | endColumn |
      | invalid/blocks/outofscope.exe | "Use of a not existing variable." | 14   | 6      | 14      | 7         |

  @comments
  Scenario Outline: Invalid comments do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                          | message                           | line | column | endLine | endColumn |
      | invalid/comments/commented_variable_declaration.exe | "Use of a not existing variable." | 3    | 4      | 3       | 10        |

  @conditionals
  Scenario Outline: Invalid conditionals do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                                 | message                           | line | column | endLine | endColumn |
      | invalid/conditionals/incorrectVariableScopeInThenBlock.exe | "Use of a not existing variable." | 5    | 4      | 5       | 5         |

  @functions
  Scenario Outline: Invalid functions do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                                | message                                                                     | line | column | endLine | endColumn |
      | invalid/foonctions/badNamedArgument.exe                   | "Cannot find a provided name of argument in function declaration (b)."      | 5    | 6      | 5       | 9         |
      | invalid/foonctions/badNumberOfArguments.exe               | "Trying to pass too many arguments to a function."                          | 5    | 4      | 5       | 11        |
      | invalid/foonctions/duplicatedNamedArgument.exe            | "Trying to pass an already provided parameter (b)."                         | 3    | 11     | 3       | 14        |
      | invalid/foonctions/duplicatedNamedArgumentTooManyArgs.exe | "Trying to pass an already provided parameter (a)."                         | 3    | 11     | 3       | 14        |
      | invalid/foonctions/undefFunctionCall.exe                  | "Call of a not existing function (g)."                                      | 1    | 4      | 1       | 7         |
      | invalid/foonctions/variableAndFunctionSameNames.exe       | "Trying to call not callable thing (g)."                                    | 4    | 4      | 4       | 7         |
      | invalid/foonctions/positionalAfterNamedArg.exe            | "Cannot use positional argument after named argument."                      | 3    | 11     | 3       | 12        |
      | invalid/foonctions/assigningToFunction.exe                | "Trying to use something that is not a variable in a variable use context." | 3    | 4      | 3       | 10        |
      | invalid/foonctions/passingFunctionToAnotherFunction.exe   | "Trying to use something that is not a variable in a variable use context." | 3    | 6      | 3       | 9         |

  @loops
  Scenario Outline: Invalid loops do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                | message                                                | line | column | endLine | endColumn |
      | invalid/loops/BreakLoopOutsideOfLoop.exe  | "Break statement must be inside a loop."               | 2    | 5      | 2       | 13        |
      | invalid/loops/BreakLoopOutsideOfRange.exe | "Break use not existing loop identifier (firstLoop)."  | 8    | 9      | 8       | 25        |
      | invalid/loops/UnknownLoopLabel.exe        | "Break use not existing loop identifier (secondLoop)." | 3    | 9      | 3       | 28        |