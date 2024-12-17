package org.exeval.cfg

import org.exeval.cfg.ffm.interfaces.CallManager
import org.exeval.cfg.interfaces.CFGNode

class ForeignCallManager: CallManager {
    override fun generate_function_call(trees: List<Tree>, result: AssignableTree?, then: CFGNode): CFGNode {
        TODO("Not yet implemented")
    }
}