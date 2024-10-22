package org.exeval.input.interfaces

interface Location {
    var line: Int
    var idx: Int
}

data class SimpleLocation(override var line: Int, override var idx: Int): Location
