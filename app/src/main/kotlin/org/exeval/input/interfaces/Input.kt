package org.exeval.input.interfaces

import org.exeval.utilities.interfaces.OperationResult

interface Input {
    var location: Location

    fun nextChar(): OperationResult<Char?>
}