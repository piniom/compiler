package org.exeval.cfg

sealed interface Tree


interface ConstantTree: Tree

data class LabelConstantTree(val label: Label) : ConstantTree

data class NumericalConstantTree(val value: Long) : ConstantTree

sealed interface AssignableTree : Tree

data class MemoryTree(val address: Tree) : AssignableTree
data class RegisterTree(val register: Register) : AssignableTree
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

data class Call(val label: Label) : Tree

data object Return : Tree

