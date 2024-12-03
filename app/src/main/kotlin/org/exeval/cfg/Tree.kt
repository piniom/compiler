package org.exeval.cfg

sealed interface Tree {
    fun kind(): TreeKind {
        return when (this) {
            is ConstantTree -> TreeKind.CONSTANT
            is MemoryTree -> TreeKind.MEMORY
            is RegisterTree -> TreeKind.REGISTER
            is AssignmentTree -> TreeKind.ASSIGNMENT
            is BinaryOperationTree -> when (this.operation) {
                BinaryTreeOperationType.ADD -> TreeKind.BINARY_ADD
                BinaryTreeOperationType.SUBTRACT -> TreeKind.BINARY_SUBTRACT
                BinaryTreeOperationType.MULTIPLY -> TreeKind.BINARY_MULTIPLY
                BinaryTreeOperationType.DIVIDE -> TreeKind.BINARY_DIVIDE
                BinaryTreeOperationType.AND -> TreeKind.BINARY_AND
                BinaryTreeOperationType.OR -> TreeKind.BINARY_OR
                BinaryTreeOperationType.GREATER -> TreeKind.BINARY_GREATER
                BinaryTreeOperationType.GREATER_EQUAL -> TreeKind.BINARY_GREATER_EQUAL
                BinaryTreeOperationType.EQUAL -> TreeKind.BINARY_EQUAL
                BinaryTreeOperationType.LESS -> TreeKind.BINARY_LESS
                BinaryTreeOperationType.LESS_EQUAL -> TreeKind.LESS_EQUAL
            }
            is UnaryOperationTree -> when (this.operation) {
                UnaryTreeOperationType.NOT -> TreeKind.UNARY_NOT
                UnaryTreeOperationType.MINUS -> TreeKind.UNARY_MINUS
            }
            is Call -> TreeKind.CALL
            Return -> TreeKind.RETURN
        }
    }
}


interface ConstantTree : Tree

data class LabelConstantTree(val label: Label) : ConstantTree

data class NumericalConstantTree(val value: Long) : ConstantTree

sealed interface AssignableTree : Tree

data class MemoryTree(val address: Tree) : AssignableTree
data class RegisterTree(val register: Register) : AssignableTree
data class AssignmentTree(val destination: AssignableTree, val value: Tree) : Tree

data class BinaryOperationTree(val left: Tree, val right: Tree, val operation: BinaryTreeOperationType) : Tree
enum class BinaryTreeOperationType {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, AND, OR, GREATER, GREATER_EQUAL, EQUAL, LESS, LESS_EQUAL
}

data class UnaryOperationTree(val child: Tree, val operation: UnaryTreeOperationType) : Tree
enum class UnaryTreeOperationType {
    NOT, MINUS
}

data class Call(val label: Label) : Tree

data object Return : Tree

