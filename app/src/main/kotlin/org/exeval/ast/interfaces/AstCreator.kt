package org.exeval.ast.interfaces

import org.exeval.ast.AstInfo
import org.exeval.parser.interfaces.ParseTree

interface AstCreator<S> {
    fun create(parseTree: ParseTree<S>): AstInfo
}
