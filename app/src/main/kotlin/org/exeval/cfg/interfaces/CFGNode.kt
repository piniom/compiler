package org.exeval.cfg.interfaces

import org.exeval.cfg.interfaces.Tree

interface CFGNode{
    val thenNode: CFGNode
    val elseNode: CFGNode? 
    val trees: List<Tree> 
}