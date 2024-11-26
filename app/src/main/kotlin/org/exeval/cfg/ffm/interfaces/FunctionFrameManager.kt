package org.exeval.ffm.interfaces

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.Tree
import org.exeval.cfg.Assignable
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.cfg.interfaces.CFGNode

interface FunctionFrameManager{
    val f: FunctionDeclaration

    fun generate_var_access(x: AnyVariable): Assignable
    fun generate_function_call(trees: List<Tree>, result: Assignable?, then: CFGNode): CFGNode 
    fun variable_to_virtual_register(x: AnyVariable): UsableMemoryCell 
    fun generate_prolog(then: CFGNode): CFGNode
    fun generate_epilouge(result: Tree?): CFGNode
}
