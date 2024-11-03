import org.exeval.ast.Type

data class Program(val functions: List<FunctionDeclaration>) : ASTNode

interface ASTNode

sealed class Expr : ASTNode

data class Block(val expressions: List<Expr>) : Expr()

sealed interface AnyVariable

sealed class VariableDeclarationBase : AnyVariable, Expr() {
    abstract val name: String
    abstract val type: Type
    abstract val initializer: Expr?
}

data class ConstantDeclaration(
    override val name: String,
    override val type: Type,
    override val initializer: Expr
) : VariableDeclarationBase()

data class MutableVariableDeclaration(
    override val name: String,
    override val type: Type,
    override val initializer: Expr? = null
) : VariableDeclarationBase()

data class Assignment(val variable: String, val value: Expr) : Expr()

sealed class Literal : Expr()
data class IntLiteral(val value: Int) : Literal()
data object NopeLiteral : Literal()
data class VariableReference(val name: String) : Expr()

data class BinaryOperation(val left: Expr, val operator: BinaryOperator, val right: Expr) : Expr()
enum class BinaryOperator {
    PLUS, MINUS, MULTIPLY, DIVIDE,
    AND, OR, EQ, GT, GTE, LT, LTE
}

data class UnaryOperation(val operator: UnaryOperator, val operand: Expr) : Expr()
enum class UnaryOperator {
    NOT, MINUS
}

data class FunctionDeclaration(
    val name: String,
    val parameters: List<Parameter>,
    val returnType: Type,
    val body: Expr
) : Expr()

data class Parameter(val name: String, val type: Type) : AnyVariable, ASTNode

data class FunctionCall(
    val functionName: String,
    val arguments: List<Argument>
) : Expr()

sealed class Argument : ASTNode
data class PositionalArgument(val expression: Expr) : Argument()
data class NamedArgument(val name: String, val expression: Expr) : Argument()

data class Conditional(
    val condition: Expr,
    val thenBranch: Expr,
    val elseBranch: Expr? = null
) : Expr()

data class Loop(
    val identifier: String?,
    val body: Block
) : Expr()

data class Break(
    val identifier: String?,
    val expression: Expr? = null
) : Expr()