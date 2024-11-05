package org.exeval.ffm.interfaces

import org.exeval.ffm.interfaces.Tree

interface CFGNode{
    val thenNode: CFGNode
    val elseNode: CFGNode 
    val trees: List<Tree> 
}