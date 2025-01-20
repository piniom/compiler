package org.exeval.utilities.interfaces

import org.exeval.input.interfaces.Location

interface LexerToken {
	val categories: Set<TokenCategory>
	val text: String
	val startLocation: Location
	val stopLocation: Location
}
