package org.exeval.ast

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import org.exeval.ast.usageAnalysis

class usageAnalysisTest{
    @Test
    fun test1(){
        assert(usageAnalysis.test1())
    }
    @Test
    fun test2(){
        assert(usageAnalysis.test2())
    }
    @Test
    fun test3(){
        assert(usageAnalysis.test3())
    }
    @Test
    fun test4(){
        assert(usageAnalysis.test4())
    }
}