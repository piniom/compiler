package org.exeval.ast

class Program(val functions: List<FunctionDeclaration>) : ASTNode

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
object NopeLiteral : Literal()


class VariableReference(val name: String) : Expr()

class BinaryOperation(val left: Expr, val operator: BinaryOperator, val right: Expr) : Expr()
enum class BinaryOperator {
    PLUS, MINUS, MULTIPLY, DIVIDE,
    AND, OR, EQ, GT, GTE, LT, LTE
}

class UnaryOperation(val operator: UnaryOperator, val operand: Expr) : Expr()
enum class UnaryOperator {
    NOT, MINUS
}

class FunctionDeclaration(
    val name: String,
    val parameters: List<Parameter>,
    val returnType: Type,
    val body: Expr
) : Expr()

class Parameter(val name: String, val type: Type) : AnyVariable, ASTNode

class FunctionCall(
    val functionName: String,
    val arguments: List<Argument>
) : Expr()

sealed class Argument : ASTNode
class PositionalArgument(val expression: Expr) : Argument()
class NamedArgument(val name: String, val expression: Expr) : Argument()

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