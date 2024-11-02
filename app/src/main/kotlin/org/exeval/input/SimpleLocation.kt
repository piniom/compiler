package org.exeval.input

import org.exeval.input.interfaces.Location

data class SimpleLocation(override var line: Int, override var idx: Int) : Location