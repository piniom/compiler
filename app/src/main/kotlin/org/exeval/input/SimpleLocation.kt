package org.exeval.input

import org.exeval.input.interfaces.Location

class SimpleLocation(override val line: Int, override val idx: Int) : Location