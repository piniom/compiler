@nameResolution @invalid @diagnostics
Feature: Const Checker diagnostics
  Verify const checker return correct diagnostics on invalid source code examples.

  @variables
  Scenario Outline: Invalid variables do cause const checker errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through const checker
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                             | message                                                        | line | column | endLine | endColumn |
      | invalid/variables/ChangingConstant.exe | "An illegall assignement to a constant variable (kRealConst)." | 2    | 5      | 2       | 21        |




