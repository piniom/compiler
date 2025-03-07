package org.exeval.cfg

import org.exeval.instructions.AssignableDest

sealed interface Register : AssignableDest {
    companion object {
        const val SIZE: Long = 8
    }
}

enum class PhysicalRegister(val name_: String) : Register {
    RAX("rax"),
    RCX("rcx"),
    RSP("rsp"),
    RBP("rbp"),
    RDX("rdx"),
    RDI("rdi"),
    RSI("rsi"),
    R8("r8"),
    R9("r9"),
    R10("r10"),
    R11("r11"),
    R12("r12"),
    R13("r13"),
    R14("r14"),
    R15("r15");

    companion object {
        fun range(): Set<PhysicalRegister> {
            return setOf(
                RAX,
                RCX,
                RBP,
                RDX,
                RDI,
                RSI,
                R8,
                R9,
                R10,
                R11,
                R12,
                R13,
                R14,
                R15,

            )
        }

    }



}

class VirtualRegister() : Register

