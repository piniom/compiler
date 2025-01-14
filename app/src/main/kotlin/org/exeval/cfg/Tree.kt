package org.exeval.cfg

sealed interface Tree {
    fun treeKind(): TreeKind {
        return when (this) {
            is ConstantTree -> ConstantTreeKind
            is MemoryTree -> MemoryTreeKind
            is RegisterTree -> RegisterTreeKind
            is AssignmentTree -> AssignmentTreeKind
            is BinaryOperationTree -> when (this.operation) {
                BinaryTreeOperationType.ADD -> BinaryAddTreeKind
                BinaryTreeOperationType.SUBTRACT -> BinarySubtractTreeKind
                BinaryTreeOperationType.MULTIPLY -> BinaryMultiplyTreeKind
                BinaryTreeOperationType.DIVIDE -> BinaryDivideTreeKind
                BinaryTreeOperationType.AND -> BinaryAndTreeKind
                BinaryTreeOperationType.OR -> BinaryOrTreeKind
                BinaryTreeOperationType.GREATER -> BinaryGreaterTreeKind
                BinaryTreeOperationType.GREATER_EQUAL -> BinaryGreaterEqualTreeKind
                BinaryTreeOperationType.EQUAL -> BinaryEqualTreeKind
                BinaryTreeOperationType.LESS -> BinaryLessTreeKind
                BinaryTreeOperationType.LESS_EQUAL -> BinaryLessEqualTreeKind
            }
            is UnaryOperationTree -> when (this.operation) {
                UnaryTreeOperationType.NOT -> UnaryNotTreeKind
                UnaryTreeOperationType.MINUS -> UnaryMinusTreeKind
            }
            is Call -> CallTreeKind
            Return -> ReturnTreeKind
        }
    }
}


interface ConstantTree : Tree

data class LabelConstantTree(val label: Label) : ConstantTree

data class NumericalConstantTree(val value: Long) : ConstantTree

data class DelayedNumericalConstantTree(val getValue: () -> Long) : ConstantTree

sealed interface AssignableTree : Tree

data class MemoryTree(val address: Tree) : AssignableTree
data class RegisterTree(val register: Register) : AssignableTree
data class AssignmentTree(val destination: AssignableTree, val value: Tree) : Tree

data class BinaryOperationTree(val left: Tree, val right: Tree, val operation: BinaryTreeOperationType) : Tree
enum class BinaryTreeOperationType {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, AND, OR, GREATER, GREATER_EQUAL, EQUAL, LESS, LESS_EQUAL;

    fun treeKind(): TreeKind {
        return when (this) {
            ADD -> BinaryAddTreeKind
            SUBTRACT -> BinarySubtractTreeKind
            MULTIPLY -> BinaryMultiplyTreeKind
            DIVIDE -> BinaryDivideTreeKind
            AND -> BinaryAndTreeKind
            OR -> BinaryOrTreeKind
            GREATER -> BinaryGreaterTreeKind
            GREATER_EQUAL -> BinaryGreaterEqualTreeKind
            EQUAL -> BinaryEqualTreeKind
            LESS -> BinaryLessTreeKind
            LESS_EQUAL -> BinaryLessEqualTreeKind
        }
    }
}



data class UnaryOperationTree(val child: Tree, val operation: UnaryTreeOperationType) : Tree
enum class UnaryTreeOperationType {
    NOT, MINUS;

    fun treeKind(): TreeKind {
        return when (this) {
           UnaryTreeOperationType.NOT -> UnaryNotTreeKind
            UnaryTreeOperationType.MINUS -> UnaryMinusTreeKind
        }
    }
}

data class Call(val label: Label) : Tree

data object Return : Tree

