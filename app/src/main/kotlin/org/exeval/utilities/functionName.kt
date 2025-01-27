package org.exeval.utilities

fun getMangledFunctionName(name: String): String {
	return if (name == TokenCategories.IdentifierEntrypoint.regex) {
		name
	}
	else "FUNCTION_${name}"
}
