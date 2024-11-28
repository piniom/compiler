package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.BinaryOperation
import org.exeval.cfg.interfaces.UsableMemoryCell

class VarAccessGenerator(private val functionFrameOffset: Tree) {
    fun generateVarAccess(cell: UsableMemoryCell): AssignableTree {
        return when (cell) {
            is UsableMemoryCell.MemoryPlace -> {
                MemoryTree(
                    BinaryOperationTree(
                        functionFrameOffset,
                        NumericalConstantTree(cell.offset),
                        BinaryTreeOperationType.SUBTRACT
                    )
                )
            }

            is UsableMemoryCell.VirtReg -> {
                RegisterTree(cell.register)
            }
        }
    }
}