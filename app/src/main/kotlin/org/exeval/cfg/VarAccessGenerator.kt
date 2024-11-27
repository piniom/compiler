package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.BinaryOperation
import org.exeval.cfg.interfaces.UsableMemoryCell

private const val BASE_POINTER_NUMBER = 5

// You can either specify function frame offset manually or leave it as null
// In the latter case it will be taken from RBP register (PhysicalRegister with id BASE_POINTER_NUMBER)
class VarAccessGenerator(val functionFrameOffset: Int? = null) {
    fun generateVarAccess(cell: UsableMemoryCell): Tree {
        return when (cell) {
            is UsableMemoryCell.MemoryPlace -> {
                val functionFrameOffsetTree = if (functionFrameOffset != null)
                    ConstantTree(functionFrameOffset)
                else
                    PhysicalRegisterTree(BASE_POINTER_NUMBER)

                MemoryTree(
                    BinaryOperationTree(
                        functionFrameOffsetTree,
                        ConstantTree(cell.offset),
                        BinaryTreeOperationType.SUBTRACT
                    )
                )
            }
            is UsableMemoryCell.VirtReg -> {
                VirtualRegisterTree(cell.idx)
            }
        }
    }
}