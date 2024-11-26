package org.exeval.cfg

sealed interface Tree

sealed interface OperandArgumentType : Tree

data class Constant(val value: Int) : OperandArgumentType

sealed interface Assignable : OperandArgumentType
sealed interface Label : OperandArgumentType

data class Memory(val address: Tree) : Assignable
sealed class Register : Assignable {
    abstract val id: Int
}

data class VirtualRegister(override val id: Int) : Register()
data class PhysicalRegister(override val id: Int) : Register()
data class Assignment(val destination: Assignable, val value: Tree) : Tree

sealed interface OperationType

data class BinaryOperation(val left: Tree, val right: Tree, val operation: BinaryOperationType) : Tree
enum class BinaryOperationType : OperationType{
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, AND, OR, XOR, GREATER, GREATER_EQUAL, EQUAL, LESS, LESS_EQUAL, ASSIGNMENT
}

data class UnaryOp(val child: Tree, val operation: UnaryOperationType) : Tree
enum class UnaryOperationType : OperationType{
    NOT, MINUS, INCREMENT, DECREMENT, CALL
}

data object Call : Tree
data object Return : Tree
enum class NullaryOperationType : OperationType{
    RETURN
}
