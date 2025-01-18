package org.exeval.instructions

object LabelIdGenerator {
	private var id = 0

	fun getId(): Int {
		id = id + 1
		return id - 1
	}
}
