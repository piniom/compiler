package org.exeval.cfg

import org.exeval.instructions.ConstantOperandArgumentType

open class Label(
	val name: String,
) : ConstantOperandArgumentType {
	object DISPLAY : Label("display")

	fun toAsm(): String = "$name:"
}
