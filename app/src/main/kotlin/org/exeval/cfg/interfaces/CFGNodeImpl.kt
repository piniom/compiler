package org.exeval.cfg.interfaces

import org.exeval.cfg.Tree

data class CFGNodeImpl(override val thenNode: CFGNode, override val elseNode: CFGNode?, override val trees: List<Tree>) : CFGNode {}
