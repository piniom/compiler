package org.exeval.cfg.interfaces

sealed interface UsableMemoryCell {
    data class VirtReg(val idx: Int): UsableMemoryCell
    data class MemoryPlace(val offset: Int): UsableMemoryCell
}