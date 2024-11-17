package org.exeval.ast

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NameResolutionTest {

    @Test
    fun `should match variable to declarations`() {
        val firstDecl = ConstantDeclaration("first_var", IntType, IntLiteral(1))
        val secondDecl = MutableVariableDeclaration("second_var", IntType, IntLiteral(2))
        val variableRef = VariableReference("first_var")
        val secondRef = VariableReference("second_var")
        val function = FunctionDeclaration(
            "main", emptyList(), IntType,
            Block(
                listOf(
                    firstDecl,
                    secondDecl,
                    ConstantDeclaration(
                        "third_var",
                        IntType,
                        BinaryOperation(variableRef, BinaryOperator.PLUS, secondRef)
                    )
                )
            )
        )
        val astInfo = AstInfo(function, emptyMap())

        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            firstDecl,
            nameResolution.variableToDecl[variableRef],
            "Expected 'first_var' to be matched to its declaration"
        )
        assertEquals(
            secondDecl,
            nameResolution.variableToDecl[secondRef],
            "Expected 'second_var' to be matched to its declaration"
        )
    }

    @Test
    fun `should match function to declarations`() {
        val call = FunctionCall("main", emptyList())
        val function = FunctionDeclaration(
            "main", emptyList(), IntType,
            Block(
                listOf(call)
            )
        )
        val astInfo = AstInfo(function, emptyMap())

        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            function,
            nameResolution.functionToDecl[call],
            "Expected 'main' function to be matched to its declaration"
        )
    }

    @Test
    fun `should match assignment to declarations`() {
        val decl = MutableVariableDeclaration("var", IntType, IntLiteral(1))
        val assignment = Assignment("var", IntLiteral(3))
        val function = FunctionDeclaration(
            "main", emptyList(), IntType,
            Block(
                listOf(
                    decl,
                    assignment
                )
            )
        )
        val astInfo = AstInfo(function, emptyMap())
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            decl,
            nameResolution.assignmentToDecl[assignment],
            "Expected 'second_var' assignment to be matched to its declaration"
        )
    }

    @Test
    fun `should match parameter to arguments`() {
        val aParam = Parameter("a", IntType)
        val bParam = Parameter("b", IntType)
        val cParam = Parameter("c", IntType)
        val dParam = Parameter("d", IntType)

        val function = FunctionDeclaration(
            "main", listOf(aParam, bParam, cParam, dParam), IntType,
            Block(
                emptyList()
            )
        )

        val aArg = PositionalArgument(IntLiteral(1))
        val bArg = PositionalArgument(IntLiteral(2))
        val cArg = NamedArgument("c", IntLiteral(3))
        val dArg = NamedArgument("d", IntLiteral(4))

        val anotherFunction = FunctionDeclaration(
            "another", listOf(), IntType,
            Block(
                listOf(
                    FunctionCall("main", listOf(
                        aArg,
                        bArg,
                        dArg,
                        cArg
                    ))
                )
            )
        )


        val program = Block(
            listOf(
                function,
                anotherFunction
            )
        )

        val astInfo = AstInfo(program, emptyMap())
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            aParam,
            nameResolution.argumentToParam[aArg],
            "Expected 'a' argument to be matched to its parameter"
        )

        assertEquals(
            bParam,
            nameResolution.argumentToParam[bArg],
            "Expected 'b' argument to be matched to its parameter"
        )

        assertEquals(
            cParam,
            nameResolution.argumentToParam[cArg],
            "Expected 'c' argument to be matched to its parameter"
        )

        assertEquals(
            dParam,
            nameResolution.argumentToParam[dArg],
            "Expected 'd' argument to be matched to its parameter"
        )
    }

    @Test
    fun `should match breaks to loops`() {
        val innerBreak = Break("inner", IntLiteral(1))
        val notNamedInner = Break(null, IntLiteral(2))
        val outerBreak = Break("outer", IntLiteral(3))
        val notNamedOuter = Break(null, IntLiteral(4))

        val innerLoop = Loop("inner", Block(listOf(innerBreak, notNamedInner, outerBreak)))
        val outerLoop = Loop("outer", Block(listOf(innerLoop, notNamedOuter)))

        val astInfo = AstInfo(outerLoop, emptyMap())
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            innerLoop,
            nameResolution.breakToLoop[innerBreak],
            "Expected 'inner' break to be matched to its loop"
        )

        assertEquals(
            innerLoop,
            nameResolution.breakToLoop[notNamedInner],
            "Expected 'inner' break to be matched to its loop"
        )

        assertEquals(
            outerLoop,
            nameResolution.breakToLoop[outerBreak],
            "Expected 'outer' break to be matched to its loop"
        )

        assertEquals(
            outerLoop,
            nameResolution.breakToLoop[notNamedOuter],
            "Expected 'outer' break to be matched to its loop"
        )
    }
}