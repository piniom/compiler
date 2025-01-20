package org.exeval.cfg.interfaces

import org.exeval.cfg.Tree

interface CFGNode {
	val branches: Pair<CFGNode, CFGNode?>?
	val trees: List<Tree>
}
