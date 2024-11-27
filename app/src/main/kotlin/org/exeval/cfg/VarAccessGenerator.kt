package org.exeval.cfg

import org.exeval.cfg.interfaces.UsableMemoryCell


class VarAccessGenerator(private val functionFrameOffset: Tree) {
    fun generateVarAccess(cell: UsableMemoryCell): Assignable {
        return when (cell) {
            is UsableMemoryCell.MemoryPlace -> {
                Memory(
                    BinaryOperation(
                        functionFrameOffset,
                        Constant(cell.offset),
                        BinaryOperationType.SUBTRACT
                    )
                )
            }
            is UsableMemoryCell.VirtReg -> {
                VirtualRegister(cell.idx)
            }
        }
    }
}