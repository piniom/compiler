package org.exeval.input.interfaces

interface Location {
    var line: Int
    var idx: Int

    fun getIndex() = this.idx
    fun getLine() = this.line
    fun setIndex(newIdx: Int) = {this.idx = newIdx}
    fun setLine(newLine: Int) = {this.line = newLine}
}

class SimpleLocation(override var line: Int, override var idx: Int): Location
