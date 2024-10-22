package org.exeval.parser.interfaces

import org.exeval.parser.Production

sealed interface ParseTree<S> {
    data class Leaf<S>(val symbol: S): ParseTree<S>
    data class Branch<S>(val production: Production<S>, val children: List<ParseTree<S>>): ParseTree<S>
}