package org.exeval.instructions

enum class InstructionKind {
    VALUE, JUMP, EXEC
}

enum class OperationType {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, AND, OR, XOR, GREATER, GREATER_EQUAL, EQUAL,
    NOT, INCREMENT, DECREMENT, CALL, RETURN
}
