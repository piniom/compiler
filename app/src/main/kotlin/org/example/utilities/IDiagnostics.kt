package org.example.utilities

import org.example.input.interfaces.ILocation

interface IDiagnostics {
    val message: String
    val startLocation: ILocation
    val stopLocation: ILocation
}