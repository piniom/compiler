package org.exeval.parser.parser.impls

import org.exeval.parser.Production

data class RealState<S>(val items: Set<LR1Item<S>>) {
    data class LR1Item<S>(val production: Production<S>, val placeholder: Int, val lookahead: S)
}

