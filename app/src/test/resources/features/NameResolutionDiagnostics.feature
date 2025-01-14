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
      | invalid/blocks/outofscope.exe | "Use of a not existing variable." | 14   | 7      | 14      | 8         |

  @comments
  Scenario Outline: Invalid comments do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                          | message                           | line | column | endLine | endColumn |
      | invalid/comments/commented_variable_declaration.exe | "Use of a not existing variable." | 3    | 5      | 3       | 11        |

  @conditionals
  Scenario Outline: Invalid conditionals do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                                 | message                           | line | column | endLine | endColumn |
      | invalid/conditionals/incorrectVariableScopeInThenBlock.exe | "Use of a not existing variable." | 5    | 5      | 5       | 6         |

  @functions
  Scenario Outline: Invalid functions do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                                | message                                                                     | line | column | endLine | endColumn |
      | invalid/foonctions/badNamedArgument.exe                   | "Cannot find a provided name of argument in function declaration (b)."      | 5    | 7      | 5       | 10        |
      | invalid/foonctions/badNumberOfArguments.exe               | "Trying to pass too many arguments to a function."                          | 5    | 5      | 5       | 12        |
      | invalid/foonctions/duplicatedNamedArgument.exe            | "Trying to pass an already provided parameter (b)."                         | 3    | 12     | 3       | 15        |
      | invalid/foonctions/duplicatedNamedArgumentTooManyArgs.exe | "Trying to pass an already provided parameter (a)."                         | 3    | 12     | 3       | 15        |
      | invalid/foonctions/undefFunctionCall.exe                  | "Call of a not existing function (g)."                                      | 1    | 5      | 1       | 8         |
      | invalid/foonctions/variableAndFunctionSameNames.exe       | "Trying to call not callable thing (g)."                                    | 4    | 5      | 4       | 8         |
      | invalid/foonctions/positionalAfterNamedArg.exe            | "Cannot use positional argument after named argument."                      | 3    | 12     | 3       | 13        |
      | invalid/foonctions/assigningToFunction.exe                | "Trying to use something that is not a variable in a variable use context." | 3    | 5      | 3       | 11        |
      | invalid/foonctions/passingFunctionToAnotherFunction.exe   | "Trying to use something that is not a variable in a variable use context." | 3    | 7      | 3       | 8         |

  @loops
  Scenario Outline: Invalid loops do cause name resolution errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through name resolution
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                | message                                                | line | column | endLine | endColumn |
      | invalid/loops/BreakLoopOutsideOfLoop.exe  | "Break statement must be inside a loop."               | 2    | 5      | 2       | 13        |
      | invalid/loops/BreakLoopOutsideOfRange.exe | "Break use not existing loop identifier (firstLoop)."  | 8    | 9      | 8       | 27        |
      | invalid/loops/UnknownLoopLabel.exe        | "Break use not existing loop identifier (secondLoop)." | 3    | 9      | 3       | 28        |