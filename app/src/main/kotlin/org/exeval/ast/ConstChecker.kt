package org.exeval.ast

import ConstantDeclaration
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.ast.NameResolution
import org.exeval.input.interfaces.Location

class ConstChecker{
    fun check(nameResolution: NameResolution, astInfo: AstInfo): List<Diagnostics>{
        
        val errors: MutableList<Diagnostics> = mutableListOf()
        nameResolution.assignmentToDecl.forEach{ (assignemt, declaration) ->
            when(declaration){
                is ConstantDeclaration -> {
                    errors.add( object: Diagnostics {
                        override val message: String = "An illegall assignement to a constant value"
                        override val startLocation: Location = astInfo.locations[declaration]!!.start
                        override val stopLocation: Location = astInfo.locations[assignemt]!!.start 
                    }) 
                }
                else -> {

                }
            }
        }
        return errors.toList() 
    }  
}