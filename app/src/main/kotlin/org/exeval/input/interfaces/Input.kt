package org.exeval.input.interfaces

interface Input {
    var location: Location

    fun nextChar(): Char?
    fun hasNextChar(): Boolean
}