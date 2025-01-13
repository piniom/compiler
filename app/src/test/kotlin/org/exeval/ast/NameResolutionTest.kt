package org.exeval.ast

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NameResolutionTest {

    @Test
    fun `should match variable to declarations`() {
        val firstDecl = ConstantDeclaration("first_var", Int, IntLiteral(1))
        val secondDecl = MutableVariableDeclaration("second_var", Int, IntLiteral(2))
        val variableRef = VariableReference("first_var")
        val secondRef = VariableReference("second_var")
        val function = FunctionDeclaration(
            "main", emptyList(), Int,
            Block(
                listOf(
                    firstDecl,
                    secondDecl,
                    ConstantDeclaration(
                        "third_var",
                        Int,
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
    fun `should match variable to declarations with pointers`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   let pointer: [Int] = new [Int] (5);
        //   pointer[0];
        //   del pointer
        // }
        // ```
        val memoryNew = MemoryNew(Array(Int), listOf(PositionalArgument(IntLiteral(5))))
        val declaration = ConstantDeclaration("pointer", Array(Int),  memoryNew)
        val reference1 = VariableReference("pointer")
        val arrayAccess = ArrayAccess(reference1, IntLiteral(0))
        val reference2 = VariableReference("pointer")
        val memoryDel = MemoryDel(reference2)
        val function = FunctionDeclaration(
            "main", emptyList(), Nope,
            Block(
                listOf(declaration, arrayAccess, memoryDel)
            )
        )
        val astInfo = AstInfo(function, emptyMap())

        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            declaration,
            nameResolution.variableToDecl[reference1],
            "Expected 'pointer` reference to be matched to its declaration when ArrayAccess"
        )

        assertEquals(
            declaration,
            nameResolution.variableToDecl[reference2],
            "Expected 'pointer` reference to be matched to its declaration when MemoryDel"
        )
    }


    @Test
    fun `should match function to declarations`() {
        val call = FunctionCall("main", emptyList())
        val function = FunctionDeclaration(
            "main", emptyList(), Int,
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
        val decl = MutableVariableDeclaration("var", Int, IntLiteral(1))
        val assignment = Assignment(VariableReference("var"), IntLiteral(3))
        val function = FunctionDeclaration(
            "main", emptyList(), Int,
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
        val aParam = Parameter("a", Int)
        val bParam = Parameter("b", Int)
        val cParam = Parameter("c", Int)
        val dParam = Parameter("d", Int)

        val function = FunctionDeclaration(
            "main", listOf(aParam, bParam, cParam, dParam), Int,
            Block(
                emptyList()
            )
        )

        val aArg = PositionalArgument(IntLiteral(1))
        val bArg = PositionalArgument(IntLiteral(2))
        val cArg = NamedArgument("c", IntLiteral(3))
        val dArg = NamedArgument("d", IntLiteral(4))

        val anotherFunction = FunctionDeclaration(
            "another", listOf(), Int,
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

    @Test
    fun `should get shadowed variable`() {
        val outerDecl = ConstantDeclaration("var", Int, IntLiteral(1))
        val innerDecl = MutableVariableDeclaration("var", Int, IntLiteral(2))
        val innerRef = VariableReference("var")
        val outerRef = VariableReference("var")

        val innerBlock = Block(listOf(innerDecl, innerRef))
        val outerBlock = Block(listOf(outerDecl, innerBlock, outerRef))

        val astInfo = AstInfo(outerBlock, emptyMap())
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            innerDecl,
            nameResolution.variableToDecl[innerRef],
            "Expected 'var' to be matched to its declaration"
        )

        assertEquals(
            outerDecl,
            nameResolution.variableToDecl[outerRef],
            "Expected 'var' to be matched to its declaration"
        )
    }

    @Test
    fun `should detect if two arguments have the same name`() {
        val ast = FunctionDeclaration(
            "main", listOf(Parameter("a", Int), Parameter("a", Int)),
            Int,
            Block(emptyList())
        )

        val astInfo = AstInfo(ast, emptyMap())

        val result = NameResolutionGenerator(astInfo).parse().diagnostics

        assertEquals(
            1,
            result.size,
            "Expected to have one error"
        )
    }

    @Test
    fun `should detect if two variables have the same name`() {
        val ast = Block(
            listOf(
                MutableVariableDeclaration("a", Int, IntLiteral(1)),
                MutableVariableDeclaration("a", Int, IntLiteral(2))
            )
        )

        val astInfo = AstInfo(ast, emptyMap())

        val result = NameResolutionGenerator(astInfo).parse().diagnostics

        assertEquals(
            1,
            result.size,
            "Expected to have one error"
        )
    }

    @Test
    fun `should detect if two loops have same identifiers`() {
        val ast = Loop("a", Loop("a", Block(emptyList())))
        val astinfo = AstInfo(ast, emptyMap())

        val result = NameResolutionGenerator(astinfo).parse().diagnostics

        assertEquals(
            1,
            result.size,
            "Expected to have one error"
        )
    }

    @Test
    fun `should detect if two arguments of foreign function have the same name`() {
        val ast = ForeignFunctionDeclaration(
            "main", listOf(Parameter("a", Int), Parameter("a", Int)),
            Int
        )

        val astInfo = AstInfo(ast, emptyMap())

        val result = NameResolutionGenerator(astInfo).parse().diagnostics

        assertEquals(
            1,
            result.size,
            "Expected to have one error"
        )
    }

    @Test
    fun `should match foreign function to declarations`() {
        val call = FunctionCall("f", emptyList())
        val decl = ForeignFunctionDeclaration("f", emptyList(), Int)

        val program =
            Program(listOf(
                decl,
                FunctionDeclaration(
                    "main", emptyList(), Int,
                    Block(
                        listOf(
                            call
                        )
                    )
                )
            )
        )

        val astInfo = AstInfo(program, emptyMap())

        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        assertEquals(
            decl,
            nameResolution.functionToDecl[call],
            "Expected 'main' function to be matched to its declaration"
        )
    }

}