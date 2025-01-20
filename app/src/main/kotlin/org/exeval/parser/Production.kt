package org.exeval.parser

data class Production<S>(
	val left: S,
	val right: List<S>,
)
