package org.exeval.utilities

import org.exeval.input.interfaces.Location
import org.exeval.utilities.interfaces.Diagnostics

data class SimpleDiagnostics(
    override val message: String,
    override val startLocation: Location,
    override val stopLocation: Location
) : Diagnostics
