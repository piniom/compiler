package org.exeval.ast

data class VariableUsage(
	val read: Set<AnyVariable>,
	val write: Set<AnyVariable>,
) {
	public fun conflicts(other: VariableUsage): Boolean {
		if (write.any { a -> other.read.contains(a) || other.write.contains(a) }) return true
		if (other.write.any { a -> read.contains(a) || write.contains(a) }) return true
		return false
	}
}
