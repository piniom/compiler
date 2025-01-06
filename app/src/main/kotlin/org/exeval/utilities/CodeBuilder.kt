package org.exeval.utilities

import org.exeval.cfg.Label
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.instructions.linearizer.BasicBlock

class CodeBuilder(val maxNestedFunctionDepth: Int) {

    private val lines: MutableList<String> = mutableListOf();

    val code: String get() = lines.joinToString ( "\n" )

    init {
        lines.add("section .data")
        lines.addAll(generateDisplay(maxNestedFunctionDepth))
        lines.add("")

        lines.addAll(
            listOf(
                "section .text",
                "global FUNCTION_main",
                ""
            )
        )
    }

    fun addFunction(
        name: String,
        blocks: List<BasicBlock>,
        registerMapping: Map<Register, PhysicalRegister>
    ) {
        lines.add("FUNCTION_$name")
        for (b in blocks) {
            lines.add(b.label.toAsm())
            lines.addAll(b.instructions.map { nested(it.toAsm(registerMapping)) })
        }
        lines.add("")
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