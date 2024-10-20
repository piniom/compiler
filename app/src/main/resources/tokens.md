# Tokens

This is the list of tokens available in our language, alongside their description in regex syntax:

```

punctuation_semicolon := ;
punctuation_arrow := ->
punctuation_monkey := @
punctuation_left_curly_bracket := {
punctuation_right_curly_bracket := }
punctuation_left_round_bracket := \(
punctuation_right_round_bracket := \)

literal_integer := (\d)(\d)*
literal_boolean := (true|false)
literal_nope := \(\)

operator_plus := +
operator_minus := -
operator_star := \*
operator_division := /

operator_or := or
operator_and := and
operator_not := not

operator_greater := >
operator_lesser := <
operator_greater_equal := >=
operator_lesser_equal := <=
operator_equal := ==
operator_not_equal := !=

operator_assign := =

keyword_if := if
keyword_then := then
keyword_else := else
keyword_loop := loop
keyword_foo := foo
keyword_break := break
keyword_return := return
keyword_let := let
keyword_mut := mut

identifier_type := \u(\l)*
identifier_nontype := (\l|_)(\i)* // captures loop labels, and function and variable names
identifier_entrypoint := main

```

## Regex syntax


Those are supported regex operators, listed by precedence:

| Symbol | Meaning                                               |
|--------|-------------------------------------------------------|
| ()     | parentheses                                           |
| *      | Kleene star                                           |
|        | concatenation                                         |
| \|     | or                                                    |

And those are available character groups:

| Symbol | Character group                                       |
|--------|-------------------------------------------------------|
| \a     | all letters (A-Za-z)                                  |
| \l     | all lowercase letters (a-z)                           |
| \u     | all upercase letters (A-Z)                            |
| \d     | all digits                                            |
| \s     | all whitespaces                                       |
| \i     | all symbols allowed in identifiers (A-Z, a-z, 0-9, _) |
