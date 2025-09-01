section .data
display:
    times 0 dd 0

section .text
global main

main:
    MOV RBP, RSP
    MOV RAX, display
    ADD RAX, 0
    MOV RAX, [RAX]
    MOV RAX, display
    ADD RAX, 0
    MOV [RAX], RBP
    MOV R9, R9
    MOV R10, R10
    MOV R11, R11
    MOV R12, R12
    MOV R13, R13
    MOV R14, R14
    MOV R15, R15
    MOV RAX, 42
    MOV RAX, 84
    MOV RAX, 0
    MOV RAX, 1
    MOV RAX, 0
    MOV RAX, display
    ADD RAX, 0
    MOV [RAX], RCX
    MOV R9, R9
    MOV R10, R10
    MOV R11, R11
    MOV R12, R12
    MOV R13, R13
    MOV R14, R14
    MOV R15, R15
    MOV RAX, 0
    RET
