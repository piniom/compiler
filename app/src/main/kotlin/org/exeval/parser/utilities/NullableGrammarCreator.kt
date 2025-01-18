package org.exeval.parser.utilities

class NullableGrammarCreator {
	fun <C> getNullable(grammar: Map<C, Set<List<C>>>): Set<C> {
		var nullable: MutableSet<C> = mutableSetOf()
		while (true) {
			val newNullable = nullable.toMutableSet()
			grammar.forEach {
				val c = it.key
				if (it.value.any {
						it.all {
							nullable.contains(it)
						}
					}
				) {
					newNullable.add(c)
				}
			}
			if (newNullable == nullable) {
				break
			} else {
				nullable = newNullable
			}
		}
		return nullable
	}
}
