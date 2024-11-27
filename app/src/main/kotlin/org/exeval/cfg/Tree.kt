package org.exeval.cfg

sealed interface Tree

sealed interface OperandArgumentTypeTree : Tree

data class ConstantTree(val value: Int) : OperandArgumentTypeTree

sealed interface AssignableTree : OperandArgumentTypeTree
sealed interface Label : OperandArgumentTypeTree

data class MemoryTree(val address: Tree) : AssignableTree
sealed class RegisterTree : AssignableTree {
    abstract val id: Int
}

data class VirtualRegisterTree(override val id: Int) : RegisterTree()
data class PhysicalRegisterTree(override val id: Int) : RegisterTree()
data class AssignmentTree(val destination: AssignableTree, val value: Tree) : Tree

sealed interface TreeOperationType

data class BinaryOperationTree(val left: Tree, val right: Tree, val operation: BinaryTreeOperationType) : Tree
enum class BinaryTreeOperationType : TreeOperationType{
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, AND, OR, XOR, GREATER, GREATER_EQUAL, EQUAL, LESS, LESS_EQUAL, ASSIGNMENT
}

data class UnaryOperationTree(val child: Tree, val operation: UnaryTreeOperationType) : Tree
enum class UnaryTreeOperationType : TreeOperationType{
    NOT, MINUS, CALL
}

data object Call : Tree
data object Return : Tree
enum class NullaryTreeOperationType : TreeOperationType{
    RETURN
}
