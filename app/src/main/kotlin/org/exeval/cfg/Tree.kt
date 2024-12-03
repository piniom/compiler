package org.exeval.cfg

sealed interface Tree {
    fun kind(): TreeKind {
        return when (this) {
            is ConstantTree -> ConstantKind
            is MemoryTree -> MemoryKind
            is RegisterTree -> RegisterKind
            is AssignmentTree -> AssignmentKind
            is BinaryOperationTree -> when (this.operation) {
                BinaryTreeOperationType.ADD -> BinaryAddKind
                BinaryTreeOperationType.SUBTRACT -> BinarySubtractKind
                BinaryTreeOperationType.MULTIPLY -> BinaryMultiplyKind
                BinaryTreeOperationType.DIVIDE -> BinaryDivideKind
                BinaryTreeOperationType.AND -> BinaryAndKind
                BinaryTreeOperationType.OR -> BinaryOrKind
                BinaryTreeOperationType.GREATER -> BinaryGreaterKind
                BinaryTreeOperationType.GREATER_EQUAL -> BinaryGreaterEqualKind
                BinaryTreeOperationType.EQUAL -> BinaryEqualKind
                BinaryTreeOperationType.LESS -> BinaryLessKind
                BinaryTreeOperationType.LESS_EQUAL -> BinaryLessEqualKind
            }
            is UnaryOperationTree -> when (this.operation) {
                UnaryTreeOperationType.NOT -> UnaryNotKind
                UnaryTreeOperationType.MINUS -> UnaryMinusKind
            }
            is Call -> CallKind
            Return -> ReturnKind
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

