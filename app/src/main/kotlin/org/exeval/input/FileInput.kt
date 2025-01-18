package org.exeval.input

import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location
import java.io.File
import java.io.InputStream

class FileInput(
	filename: String,
) : Input {
	private val file = filename
	private var idx = 0
	private var line = 0

	private val splitReg = Regex("(?<=;)|(?=;)")

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
			lineList.add(it)
		}

		return lineList
	}

	val lines: List<String> = readFile()

	override fun nextChar(): Char? {
		if (isAfterLastLine()) return null
		var currentLine = lines[line]

		while (idx >= currentLine.length) {
			idx = 0
			++line

			if (isAfterLastLine()) return null else currentLine = lines[line]
			return '\n'
		}

		var c = currentLine[idx]
		++idx

		return c
	}

	private fun isAfterLastLine(): Boolean = line >= lines.size
}
