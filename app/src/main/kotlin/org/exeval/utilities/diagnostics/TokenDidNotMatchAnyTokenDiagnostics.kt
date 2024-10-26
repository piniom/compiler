package org.exeval.utilities.diagnostics

import org.exeval.input.interfaces.Location
import org.exeval.utilities.interfaces.Diagnostics

sealed class TextDidNotMatchAnyTokensDiagnostics(
    override val message: String,
    override val startLocation: Location,
    override val stopLocation: Location
) : Diagnostics {

    data class ValidTextDidNotMatchAnyTokensDiagnostics(
        override val message: String,
        override val startLocation: Location,
        override val stopLocation: Location
    ) : TextDidNotMatchAnyTokensDiagnostics(message, startLocation, stopLocation)

    companion object {
        fun create(text: String, startLocation: Location, stopLocation: Location): TextDidNotMatchAnyTokensDiagnostics {
            return ValidTextDidNotMatchAnyTokensDiagnostics(
                "String \"$text\" didn't match any tokens!",
                startLocation,
                stopLocation
            )
        }
    }
}