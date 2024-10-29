package org.exeval.ast

import AnyVariable

data class VariableUsage(
    val read: List<AnyVariable>, val write: List<AnyVariable>
)

