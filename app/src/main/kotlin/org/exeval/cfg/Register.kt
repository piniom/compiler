package org.exeval.cfg

sealed interface Register{
    companion object {
        const val SIZE: Long = 8
    }
}

enum class PhysicalRegister(val name_: String) : Register {
    RAX("rax"),
    RCX("rcx"),
    RSP("rsp"),
    RBP("rbp"),
    RDX("rdx");
}

class VirtualRegister(): Register

