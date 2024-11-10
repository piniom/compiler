package org.exeval.ast

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.exeval.ast.usageAnalysis

class varusage{
    @Test
    fun upwardPropagationTest(){
        assert(usageAnalysis.test1())
    }
    @Test
    fun argParamMappingTest(){
        assert(usageAnalysis.test2())
    }
    @Test
    fun functionPropagationTest(){
        assert(usageAnalysis.test3())
    }
}