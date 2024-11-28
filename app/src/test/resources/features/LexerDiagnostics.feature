@lexer @invalid @diagnostics
Feature: Lexer diagnostics
  Verify lexer return correct diagnostics on invalid source code examples.

  @comments
  Scenario Outline: Invalid comments do cause lexer errors
    Given ExEval source code file "<sourceFile>"
    When source code is passed through lexer
    Then returns diagnostic with message <message> that starts at line <line> and column <column> and ends at line <endLine> and column <endColumn>
    Examples:
      | sourceFile                                | message                                                       | line | column | endLine | endColumn |
      | invalid/comments/not_finished_comment.exe | "Comment has not been finished at the end of the input file." | 5    | 0      | 5       | 0         |
