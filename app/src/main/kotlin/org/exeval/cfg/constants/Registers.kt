package org.exeval.cfg.constants

import org.exeval.cfg.Tree
import org.exeval.cfg.PhysicalRegister

object Registers {
    val registerSize = 8,
    val RAX = PhysicalRegister(0),
    val RCX = PhysicalRegister(3),
    val RSP = PhysicalRegister(4),
    val RDX = PhysicalRegister(8)
}
