package org.exeval.cfg.interfaces

import org.exeval.cfg.Register

sealed interface UsableMemoryCell {
	data class VirtReg(
		val register: Register,
	) : UsableMemoryCell

	data class MemoryPlace(
		val offset: Long,
	) : UsableMemoryCell
}
