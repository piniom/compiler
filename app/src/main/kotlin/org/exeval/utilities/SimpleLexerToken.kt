package org.exeval.utilities

import org.exeval.input.interfaces.Location
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.TokenCategory

data class SimpleLexerToken(
	override val categories: Set<TokenCategory>,
	override val text: String,
	override val startLocation: Location,
	override val stopLocation: Location,
) : LexerToken
