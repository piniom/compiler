package org.exeval.cfg.interfaces

import org.exeval.cfg.Tree

interface CFGNode{
    val thenNode: CFGNode
    val elseNode: CFGNode? 
    val trees: List<Tree> 
}