package org.exeval.parser

import kotlinx.serialization.Serializable

@Serializable
data class Production<S> (
    val left: S,
    val right: List<S>,
)