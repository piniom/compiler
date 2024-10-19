# Tokens

This is the list of tokens available in our language, alongside their description in regex syntax:

```

Punctuation:

punctuation_semicolon := ;
punctuation_arrow := ->
punctuation_monkey := @
punctuation_left_curly_bracket := {
punctuation_right_curly_bracket := }
punctuation_left_round_bracket := \(
punctuation_right_round_bracket := \)

literal_integer := (\d)*
literal_boolean := (true|false)

operator_plus := +
operator_minus := -
operator_star := \*
operator_division := /

operator_or := or
operator_and := and
operator_not := not

operator_greater := >
operator_lesser := <
operator_greater_equal := <=
operator_lesser_equal := >=
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

identifier_type := \u(\i)*
identifier_general := (\l|_)(\i)*
identifier_entrypoint := main

```
