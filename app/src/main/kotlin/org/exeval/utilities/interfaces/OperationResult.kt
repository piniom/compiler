package org.exeval.utilities.interfaces

data class OperationResult<T>(
	val result: T,
	val diagnostics: List<Diagnostics>,
)
