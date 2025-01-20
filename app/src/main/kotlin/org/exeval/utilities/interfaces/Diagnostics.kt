package org.exeval.utilities.interfaces

import org.exeval.input.interfaces.Location

interface Diagnostics {
	val message: String
	val startLocation: Location
	val stopLocation: Location
}
