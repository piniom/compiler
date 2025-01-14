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


  @various
  Scenario: Invalid programs using various features do cause lexer errors
    Given ExEval source code file "invalid/various/InvalidTokens.exe"
    When source code is passed through lexer
    Then returns diagnostics:
      | message                             | line | column | endLine | endColumn |
      | String "~" didn't match any tokens! | 1    | 3      | 1       | 4         |
      | String "$" didn't match any tokens! | 2    | 3      | 2       | 4         |
      | String "^" didn't match any tokens! | 3    | 5      | 3       | 5         |

