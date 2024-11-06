package org.exeval.ast

data class VariableUsage(
    val read: Set<AnyVariable>, val write: Set<AnyVariable>
)

