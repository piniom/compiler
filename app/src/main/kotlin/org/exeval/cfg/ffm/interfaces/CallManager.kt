package org.exeval.cfg.ffm.interfaces

import org.exeval.cfg.AssignableTree
import org.exeval.cfg.Tree
import org.exeval.cfg.interfaces.CFGNode

interface CallManager {
    fun generate_function_call(trees: List<Tree>, result: AssignableTree?, then: CFGNode): CFGNode
}