package org.exeval.ast

import AnyVariable

data class VariableUsage(
    val read: Set<AnyVariable>, val write: Set<AnyVariable>
)

