package org.exeval.cfg

sealed interface Register {
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
    R8("r8"),
    R9("r9"),
    R10("r10"),
    R11("r11"),
    R12("r12"),
    R13("r13"),
    R14("r14"),
    R15("r15");

}

class VirtualRegister() : Register

