package org.exeval.cfg

import org.exeval.cfg.Tree
import org.exeval.cfg.interfaces.CFGNode

data class CFGNodeImpl(
    override val branches: Pair<CFGNode,CFGNode?>?,
    override val trees: List<Tree>
) : CFGNode {}
