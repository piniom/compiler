package org.exeval.input

import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location
import org.exeval.input.interfaces.SimpleLocation
import java.io.File
import java.io.InputStream

class FileInput(filename: String) : Input {
    private val file = filename
    private var idx = -1
    private var line = 0

    private val splitReg = Regex("(?<=,)|(?=,)")

    override var location: Location
        get() {
            return SimpleLocation(line, idx)
        }
        set(value) {
            idx = value.idx
            line = value.line
        }

    fun readFile(): List<String> {
        val inputStream: InputStream = File(file).inputStream()
        val lineList = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine {
            val lines = it.split(splitReg)
            for (line in lines) {
                lineList.add(line)
            }
        }

        return lineList
    }

    val lines: List<String> = readFile()

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