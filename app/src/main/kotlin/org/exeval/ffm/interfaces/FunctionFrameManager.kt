package org.exeval.ffm.interfaces

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionDeclaration
import org.exeval.ffm.interfaces.Tree
import org.exeval.ffm.interfaces.VirtReg

interface FunctionFrameManager{

    fun generate_var_access(f: FunctionDeclaration, x: AnyVariable): Tree
    fun generate_function_call(trees: List<Tree>, f: FunctionDeclaration, then: CFGNode): CFGNode 
    fun variable_to_virtual_register(x: AnyVariable): VirtReg 
    fun generate_prolog(f: FunctionDeclaration, then: CFGNode): CFGNode
    fun generate_epilouge(f: FunctionDeclaration, result: Tree): CFGNode
}