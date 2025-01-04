package org.exeval.ast

class Program(val functions: List<AnyFunctionDeclaration>) : ASTNode

interface ASTNode

sealed class Expr : ASTNode

class Block(val expressions: List<Expr>) : Expr()

sealed interface AnyVariable : ASTNode

sealed class VariableDeclarationBase : AnyVariable, Expr() {
    abstract val name: String
    abstract val type: Type
    abstract val initializer: Expr?
}

class ConstantDeclaration(
    override val name: String,
    override val type: Type,
    override val initializer: Expr
) : VariableDeclarationBase()

class MutableVariableDeclaration(
    override val name: String,
    override val type: Type,
    override val initializer: Expr? = null
) : VariableDeclarationBase()

class Assignment(val variable: String, val value: Expr) : Expr()

sealed class Literal : Expr()
class IntLiteral(val value: Long) : Literal()

class BoolLiteral(val value: Boolean) : Literal()
class NopeLiteral : Literal()


class VariableReference(val name: String) : Expr()

class BinaryOperation(val left: Expr, val operator: BinaryOperator, val right: Expr) : Expr()
enum class BinaryOperator {
    PLUS, MINUS, MULTIPLY, DIVIDE,
    AND, OR, EQ, GT, GTE, LT, LTE, NEQ
}

class UnaryOperation(val operator: UnaryOperator, val operand: Expr) : Expr()
enum class UnaryOperator {
    NOT, MINUS
}

sealed class AnyFunctionDeclaration() : Expr()
{
    abstract val name: String;
    abstract val parameters: List<Parameter>;
    abstract val returnType: Type
}

class FunctionDeclaration(
    override val name: String,
    override val parameters: List<Parameter>,
    override val returnType: Type,
    val body: Expr
) : AnyFunctionDeclaration()

class ForeignFunctionDeclaration(
    override val name: String,
    override val parameters: List<Parameter>,
    override val returnType: Type
) : AnyFunctionDeclaration()

class Parameter(val name: String, val type: Type) : AnyVariable, ASTNode

class FunctionCall(
    val functionName: String,
    val arguments: List<Argument>
) : Expr()

sealed class Argument(val expression: Expr) : ASTNode
class PositionalArgument(expression: Expr) : Argument(expression)
class NamedArgument(val name: String, expression: Expr) : Argument(expression)

class Conditional(
    val condition: Expr,
    val thenBranch: Expr,
    val elseBranch: Expr? = null
) : Expr()

class Loop(
    val identifier: String?,
    val body: Expr
) : Expr()

class Break(
    val identifier: String?,
    val expression: Expr? = null
) : Expr()

class MemoryNew(
    val type: Type,
    val constructorArguments: List<Argument>
) : Expr()

class MemoryDel(
    val pointer: Expr
) : Expr()

class ArrayAccess(
    val array: Expr,
    val index: Expr
) : Expr()
