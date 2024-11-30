package org.exeval.cfg

import org.exeval.instructions.OperandArgumentType

open class Label(val name: String): OperandArgumentType {
    object DISPLAY : Label("display")
}
