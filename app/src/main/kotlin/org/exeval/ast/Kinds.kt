package org.exeval.ast

class Program(val functions: List<AnyFunctionDeclaration>) : ASTNode

interface ASTNode

sealed class Expr : ASTNode

sealed class AssignableExpr : Expr()

class Block(val expressions: List<Expr>) : Expr()

sealed interface TypeNode : ASTNode

data object Int : TypeNode
data object Nope : TypeNode
data object Bool : TypeNode
data class Array(val elementType: TypeNode): TypeNode

sealed interface AnyVariable : ASTNode

sealed class VariableDeclarationBase : AnyVariable, Expr() {
    abstract val name: String
    abstract val type: TypeNode
    abstract val initializer: Expr?
}

class ConstantDeclaration(
    override val name: String,
    override val type: TypeNode,
    override val initializer: Expr
) : VariableDeclarationBase()

class MutableVariableDeclaration(
    override val name: String,
    override val type: TypeNode,
    override val initializer: Expr? = null
) : VariableDeclarationBase()

class Assignment(val variable: AssignableExpr, val value: Expr) : Expr()

sealed class Literal : Expr()
class IntLiteral(val value: Long) : Literal()

class BoolLiteral(val value: Boolean) : Literal()
class NopeLiteral : Literal()
class NothingLiteral : Literal()


class VariableReference(val name: String) : AssignableExpr()

class BinaryOperation(val left: Expr, val operator: BinaryOperator, val right: Expr) : Expr()
enum class BinaryOperator {
    PLUS, MINUS, MULTIPLY, DIVIDE,
    AND, OR, EQ, GT, GTE, LT, LTE, NEQ
}

class UnaryOperation(val operator: UnaryOperator, val operand: Expr) : Expr()
enum class UnaryOperator {
    NOT, MINUS
}

sealed class AnyCallableDeclaration() : Expr()
{
    abstract val parameters: List<Parameter>;
}

sealed class AnyFunctionDeclaration() : AnyCallableDeclaration()
{
    abstract val name: String;
    abstract override val parameters: List<Parameter>;
    abstract val returnType: TypeNode
}

class FunctionDeclaration(
    override val name: String,
    override val parameters: List<Parameter>,
    override val returnType: TypeNode,
    val body: Expr
) : AnyFunctionDeclaration()

class ForeignFunctionDeclaration(
    override val name: String,
    override val parameters: List<Parameter>,
    override val returnType: TypeNode
) : AnyFunctionDeclaration()

class Parameter(val name: String, val type: TypeNode) : AnyVariable, ASTNode

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
    val type: TypeNode,
    val constructorArguments: List<Argument>
) : Expr()

class MemoryDel(
    val pointer: Expr
) : Expr()

class ArrayAccess(
    val array: Expr,
    val index: Expr
) : AssignableExpr()

class StructTypeDeclaration(
    val name: String,
    val fields: List<VariableDeclarationBase>,
    val constructorMethod: ConstructorDeclaration
) : Expr()

class ConstructorDeclaration(
    override val parameters: List<Parameter>,
    val body: Expr
) : AnyCallableDeclaration()

class StructFieldAccess(
    val structObject: Expr,
    val field: String
) : AssignableExpr()

class HereReference(val name: String?) : AssignableExpr()

class TypeUse(
    val typeName: String
) : TypeNode
