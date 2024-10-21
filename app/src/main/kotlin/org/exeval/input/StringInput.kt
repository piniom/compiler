package org.exeval.input

import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location

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

    override fun nextChar(): Char? {
        if (isAfterLastLine()) return null
        var currentLine = lines[line]

        ++idx
        while (idx >= currentLine.length) {
            idx = 0
            ++line

            if (isAfterLastLine()) return null
            currentLine = lines[idx]
        }

        return currentLine[idx]
    }

    private fun isAfterLastLine(): Boolean {
        return line >= lines.size
    }
}
