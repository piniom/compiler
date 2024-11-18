package org.exeval.cfg

sealed interface Tree

data class Constant(val value: Int) : Tree

sealed interface Assignable : Tree
sealed interface Label : Tree

data class Memory(val address: Tree) : Assignable
sealed class Register : Assignable {
    abstract val id: Int
}

data class VirtualRegister(override val id: Int) : Register()
data class PhysicalRegister(override val id: Int) : Register()
data class Assigment(val destination: Assignable, val value: Tree) : Tree

data object Call : Tree
data object Return : Tree

data class BinaryOperation(val left: Tree, val right: Tree, val operation: BinaryOperationType) : Tree
enum class BinaryOperationType {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, AND, OR, XOR, GREATER, GREATER_EQUAL, EQUAL
}

data class UnaryOp(val child: Tree, val binaryOperationType: UnaryOperationType) : Tree
enum class UnaryOperationType {
    NOT, INCREMENT, DECREMENT
}
