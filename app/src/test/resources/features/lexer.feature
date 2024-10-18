@lexer
Feature: Lexer errors
	Verify errors, or lack thereof, returned by lexer on valid and invalid source code examples.

	@valid @blocks @notImplemented
	Scenario Outline: Valid blocks do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                    |
			| valid/blocks/blockinblock.exe |
			| valid/blocks/limitscope.exe   |
			| valid/blocks/max.exe          |

	@valid @comments @notImplemented
	Scenario Outline: Valid comments do not cause lexer errors
		Given ExEval source code file "<sourceFile>"
		When source code is passed through lexer
		Then no errors are returned
		Examples:
			| sourceFile                              |
			| valid/comments/mixing_comments.exe      |
			| valid/comments/multi_line_comments.exe  |
			| valid/comments/single_line_comments.exe |
