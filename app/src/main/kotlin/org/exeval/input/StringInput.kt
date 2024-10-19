package org.exeval.input

import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location
import org.exeval.utilities.interfaces.OperationResult

class StringInput(inputStr: String) : Input {
    private var idx = -1
    private var line = 0

    private val splitReg = Regex("(?<=,)|(?=,)")
    private val lines = inputStr.split(splitReg)

    override var location: Location
        get() {
            return SimpleLocation(line, idx)
        }
        set(value) {
            idx = value.idx
            line = value.line
        }

    override fun nextChar(): OperationResult<Char?> {
        if (isAfterLastLine()) return getEndResult()
        var currentLine = lines[line]

        ++idx
        while (idx >= currentLine.length) {
            idx = 0
            ++line

            if (isAfterLastLine()) return getEndResult()
            currentLine = lines[idx]
        }

        return getCharResult(currentLine[idx])
    }

    private fun isAfterLastLine(): Boolean {
        return line >= lines.size
    }

    private fun getEndResult(): OperationResult<Char?> {
        return OperationResult(null, emptyList())
    }

    private fun getCharResult(char: Char): OperationResult<Char?> {
        return OperationResult(char, emptyList())
    }
}
