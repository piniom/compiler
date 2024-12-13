package org.exeval.utilities

import org.exeval.cfg.Label
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.instructions.linearizer.BasicBlock

class CodeBuilder() {
    fun generate(
        linearizedFunctions: List<Pair<String, List<BasicBlock>>>,
        maxNestedFunctionDepth: Int,
        registerMapping: Map<Register, PhysicalRegister>
    ): List<String> {
        val code = mutableListOf<String>()

        code.add("section .data")
        code.addAll(generateDisplay(maxNestedFunctionDepth))
        code.add("")

        code.addAll(
            listOf(
                "section .text",
                "global FUNCTION_main",
                ""
            )
        )

        for ((name, blocks) in linearizedFunctions) {
            code.add("FUNCTION_$name")
            for (b in blocks) {
                code.add(b.label.toAsm())
                code.addAll(b.instructions.map { nested(it.toAsm(registerMapping)) })
            }
            code.add("")
        }

        return code
    }

    private fun generateDisplay(maxDepth: Int): List<String> {
        val display = Label.DISPLAY
        return listOf(
            display.toAsm(),
            nested("times $maxDepth dd 0")
        )
    }

    private fun nested(s: String): String {
        return "    $s"
    }
}