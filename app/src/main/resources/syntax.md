# Syntax of the [name-needed] language

## Blocks
The blocks are defined by curly braces `{}`.

## Identifiers
There are two naming rules **enforced** by the **language**:
- `variables` and `functions` start with a lower case letter:
```
variableName
functionName
```
- `types` start with an upper case letter:
```
TypeName
```

## Statements and expressions
Every `statement` is an `expression`!

`STATEMENT = EXPRESSION`

## Separator
`;` is used as an expression separator. 

Eg.:
```
{ (expr); (expr); (expr) }
```
Since every block is an expression the value of the block is the value of the last expression.

`(expr)` denotes an arbitrary expression and may not be a valid language statement.

## Variables
- Each `variable` is **immutable** by default. 
- `let` and `let mut` are used to declare a `constant` and a `variable` respectively.
- `Variables` may and `constants` must be initialized upon declaration.
- The declaration `keyword` is followed by the `name` of the `variable` and its `type`.
- The value of the declaration expression and assigment expression is `()`. 

Examples of valid declarations:
```
let x: Int = 0;
let mut y: Nope = ();
let mut z: Int;
z = 1;
```
## Functions
- Names of the arguments are the function's public contract.
- The body of a function is an expression.
- Default values are not allowed.
- No overloading.
- `foo` is the function declaration keyword.
- The value of the function definition expression is `()`.
- Examples of the function definition:
```
foo functionName1(arg1: Type1, arg2: Type2) -> ReturnType = 2;
foo functionName2(arg1: Type1, arg2: Type2) -> ReturnType = {

}
```
- The arguments may be passed without explicitly specifying the names of the arugments and the order of the definition is assumed.
- If one argument is named, then all arguments are too. When naming arguments, any order is allowed.
- Examples of the function invocation: 
```
functionName1(0, ());
functionName2(arg1 = 0, arg2 = ());
functionName2(arg2 = (), arg1 = 0);
```
- Declaring functions within functions is allowed
```
foo functionName4() -> ReturnType = {
    ...
    foo functionName5() -> Nope = ();
    ...
}
```
- Child functions may not return from the parent.


## Types
There are only 3 `Types`:
- `Int` - numerical values.
- `Nope` - unit/empty type, with `()` as its only value.
- `Bool` - logical value `true/false`.


## Conditionals
- The conditionals use `if`, `then`, and `else` keywords with `expressions` in-between. 
- The `expression` after the `if` keyword must evaluate to a `Bool` type.
- `then` and `else` blocks must evaluate to the same type.
- If no `else` expression is provided the `then` expression must evaluate to `()`.
```
if true then 1 else 2;

if (expr) then {
    (expr);
    (expr);
    true
} else false;

if (expr) then {
    (expr);
    ()
}
```

## Loops

- There is only one loop called `loop`.
- Breaking is done by using `break (expr);` or `break;` which is equivalent to `break ();`.
- The value of the loop is the value of the expression after the `break`.
- All `break` expressions must evaluate to the same type.
- Loops have identifiers `loop@identifier`.
- Breaking out of the specific `loop` in a nested `loop` scenario is done by using `break@identifier`.
- Loop identifiers follow the same naming rules as `variables` and `functions`.

## `and`, `or`, `not` with shortcircuit
Shortcuting means that the second expression is not evaluated if the resulting value is already determined by the first one.

## Entrypoint
Every program must have a `main` function declared. It's the entrypoint to the program. The `main` entrypoint takes no arguments and must return an `Int`. 
```
foo main() -> Int = 0;
```

## Comments
Single line `//` and multiline `/* some text */` comments are allowed.

## Comparison operators 
`==` is used to test for equality.

Additionaly the `Int` type supports `>`, `>=`, `<`, `<=` with standard behaviour.


