# ExEval formal grammar

A grammatically-correct program file consists only of function and structures definitions:

```
<program> ::= <Top level statements delcarations>
<Top level statement delcarations> ::= <function declaration> | <function declaration> <Top level statement delcarations> 
	| <structure declaration> | <structure declaration> <Top level statement delcarations>
```

Function is defined with `foo` keyword. It may take zero or more arguments. In the first case,
it's brackets are indistinguishable from the `Nope` literal (unless they're separated by a space).
Entrypoint is a special case of function.

```
<function declaration> ::= <foo> <identifier> <left round bracket> <function params> <right round bracket> <arrow> <type> <assign> <expression>
	| <foo> <identifier> <nope> <arrow> <type> <assign> <expression>
	| <foo> <identifier> <left round bracket> <right round bracket> <arrow> <type> <assign> <expression>
	| <foo> <identifier entrypoint> <nope> <arrow> <type> <assign> <expression>
	| <foo> <identifier entrypoint> <left round bracket> <right round bracket> <arrow> <type> <assign> <expression>
// <function declaration> ::= foo <identifier> ( <function params> ) -> <type> = <expression>
//	| foo <identifier> () -> <type> = <expression>
//	| foo <identifier> ( ) -> <type> = <expression>
//	| foo <identifier entrypoint> () -> <type> = <expression>
//	| foo <identifier entrypoint> ( ) -> <type> = <expression>

<function params> ::= <function param> | <function param> <comma> <function params>
<function param> ::= <identifier> <colon> <type>

<constructor params> = <ctor> <left curly bracket> <function params> <right curly bracket> <assign> | <ctor> <literalNope> <assign> 
<constructor declaration> = <constructor params> <expression> 

```

Functions can be called both with named and unnamed arguments. Grammar does not enforce that
all arguments are passed in the same way. From grammar's point of view, assignment is indistinguishable
from a named argument, so no distinction is made. An argument can be any expression.

```
<function call> ::= <identifier> <left round bracket> <function call arguments> <right round bracket>
	| <identifier> <nope> | <identifier> <left round bracket> <right round bracket>
<function call arguments> ::= <expression> | <expression> <comma> <functoin call arguments>
```

Allocation & Deallocation

```
<alloc> ::= <new> <type> <left round bracket> <function call> <right round bracket>
<dealloc> ::= <del> <expression>
```

Structures

```
<struct definition> ::= <uct> <type> <assign> <left curly bracket> <struct definition body> <right curly bracket>
<struct definition body> ::= <struct definition body property> | <struct definition body property> <semicolon> <struct definition body>
	| <struct definition body property> <struct definition body>

<struct definition body property> ::= <variable declaration> | <constant declaration> | <constructor declaration>
	| <let> <identifier> <colon> <type>

<here access> ::= <here> | <here> <dot> <struct access> | <here> <dot> <identifier> | <here> <dot> <array access>

<struct access> ::= <array access> <dot> <identifier> | <array access> <dot> <array access> | <array access> <dot> <sturct access>
	| <identifier> <dot> <identifier> | <identifier> <dot> <array access> | <identifier> <dot> <struct access>
	| <function call> <dot> <struct access> | <function call> <dot> <identifier> | <function call> <dot> <array access>
```

Arrays are used like in most common languages

```
<array index> ::= <left square bracket> <expression> <right square bracket> | <left square bracket> <expression> <right square bracket> <array index>
<array access> ::= <identifier> <array index> | <function call> <array index> | <left round bracket> <expression> <right round bracket> <array index>

```

Expressions are divided into two types: those that need to end with a semicolon and those that don't.
In the first category fall:

- `if` without an `else`, as otherwise derivation of `if ... then ... if ... then ... else ...` is unambiguous,
- `break` without an expression (implicitly returning `Nope`), as `break <expression>` would be unambiguous.

However this distinction should not be visible outside, as in fact semicolon is not demanded when it is
the last expression in a block, and all expressions in a block have to be separated by a semicolon.

```
<expression> ::= <simple expression> | <expression with semicolon> | <expression block>
<expression block> ::= <left curly bracket> <expression chain> <right curly bracket>
	| <left curly bracket> <last expression in block> <right curly bracket>
	| <left curly bracket> <expression chain> <semicolon> <last expression in block> <right curly bracket>
<expression chain> ::= <simple expression> | <expression with semicolon> | <expression block>
	| <simple expression> <error> <semicolon> <expression chain>
	| <expression with semicolon> <expression chain>
	| <expression block> <error> <semicolon> <expression chain>
<simple expression> ::= <value> | <identifier> | <arithmetic expression> | <variable declaration>
	| <constant declaration> | <variable assignment> | <function declaration> | <functoin call>
	| <if then else> | <loop> | <break expression> | <array access> | <structure access> | <here access>
<expression with semicolon> ::= <if then> | <break keyword>
<last expression in block> ::= <if then without semicolon> | <break keyword without semicolon>
```

Arithmetic expression is a special case of expression. It always contains an operator, to be
distinguished from the general expression. To keep grammar unambiguous, both branches of a 2-ary operator
cannot be expressions. Assignment is not considered an arithmetic expression.

```
<arithmetic expression> ::= <value> <2-ary operator> <expression>
	| <identifier> <2-ary operator> <expression> | <function call> <2-ary operator> <expression>
	| <1-ary operator> <expression> | <left round bracket> <expression> <right round bracket>

<2-ary operator> ::= <plus> | <minus> | <star> | <division> | <or> | <and> | <greater> | <less>
	| <greater or equal> | <less or equal> | <equal> | <not equal>
// <2-ary operator> ::= + | - | * | / | or | and | > | < | >= | <= | == | !=

<1-ary operator> ::= <minus> | <not>
// <1-ary operator> ::= - | not
```

`if` and `break` come in a couple of flavors, due to the ambiguity problem caused by optional parts.
`loop`'s optional label luckily doesn't pose such problems. All of these can take either a single
expression, or an expression block.

```
<if then else> ::= <if> <expression> <then> <expression> <else> <expression>
<if then> ::= <if> <expression> <then> <expression> <error> <semicolon>
<if then without semicolon> ::= <if> <expression> <then> <expression>

<loop> ::= <keyword loop> <expression> | <keyword loop> <at> <identifier> <expression>
// <loop> ::= loop <expression> | loop @ <identifier> <expression>

<break expression> ::= <break> <expression> | <break> <at> <identifier> <expression>
<break keyword> ::= <break> <error> <semicolon> | <break> <at> <identifier> <error> <semicolon>
<break keyword without semicolon> ::= <break> | <break> <at> <identifier>
```

Value is considered to be one of three types: an integer, a boolean, or `Nope`. Requirement for constants
to be assigned a value at declaration is enforced by grammar. Only variables can be assigned
(as understood by grammar - that is identifiers).

```
<value> ::= <integer> | <boolean> | <nope>

<variable declaration> ::= <let> <mut> <identifier> <colon> <type>
	| <let> <mut> <identifier> <colon> <type> <assign> <expression>
<constant declaration> ::= <let> <identifier> <colon> <assign> <expression>

<variable assignment> ::= <identifier> <assign> <expression> | <array access> <assign> <expression>
	| <struct access> <assign> <expression> | <here access> <assign> <expression>
```

There is one special terminal, `<error>`, which does not correspond to any token. It's sole purpose is
for parser to skip problematic input program fragments until this symbol. All other terminals
correspond to tokens defined in [TokenCategories](../app/src/main/kotlin/org/exeval/utilities/TokenCategories.kt).

```
<error> ::=
<semicolon> = PunctuationSemicolon
<colon> = PunctuationColon
<comma> = PunctuationComma
<arrow> = PunctuationArrow
<dot> = PunctuationDot
<at> = PunctuationMonkey
<left curly bracket> = PunctuationLeftCurlyBracket
<right curly bracket> = PunctuationRightCurlyBracket
<left round bracket> = PunctuationLeftRoundBracket
<right round bracket> = PunctuationRightRoundBracket
<left square bracket> = PunctuationLeftSquareBracket
<right square bracket> = PunctuationRightSquareBracket
<integer> = LiteralInteger
<boolean> = LiteralBoolean
<nope> = LiteralNope
<plus> = OperatorPlus
<minus> = OperatorMinus
<star> = OperatorStar
<division> = OperatorDivision
<or> = OperatorOr
<and> = OperatorAnd
<not> = OperatorNot
<greater> = OperatorGreater
<less> = OperatorLesser
<greater or equal> = OperatorGreaterEqual
<less or equal> = OperatorLesserEqual
<equal> = OperatorEqual
<not equal> = OperatorNotEqual
<assign> = OperatorAssign
<if> = KeywordIf
<then> = KeywordThen
<else> = KeywordElse
<keyword loop> = KeywordLoop
<foo> = KeywordFoo
<break> = KeywordBreak
<let> = KeywordLet
<mut> = KeywordMut
<identifier> = IdentifierNontype
<type> = IdentifierType
<identifier entrypoint> = IdentifierEntrypoint
<here> = KeywordHere
<ctor> = KeywordCtor
<uct> = KeywordUct
<nothing> = LiteralNothing
<new> = KeywordNew
<del> = KeywordDel
```
