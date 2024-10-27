package org.exeval.parser.interfaces

import org.exeval.input.SimpleLocation
import org.exeval.input.interfaces.Location
import org.exeval.parser.Production

sealed interface ParseTree<S> {
    val startLocation: Location
    val endLocation: Location

    data class Leaf<S>(val symbol: S, override val startLocation: Location, override val endLocation: Location): ParseTree<S>

    data class Branch<S>(
        val production: Production<S>,
        val children: List<ParseTree<S>>,
        override val startLocation: Location,
        override val endLocation: Location
    ): ParseTree<S>
}