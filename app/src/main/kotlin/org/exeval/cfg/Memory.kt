package org.exeval.cfg

import org.exeval.instructions.AssignableDest
import org.exeval.instructions.OperandArgumentType

/* TODO make address type more general, so perhaps a more complex instruction
 *      can be used to access memory, not necessairly calculating the address first
 */
class Memory(
	val address: OperandArgumentType,
) : AssignableDest
