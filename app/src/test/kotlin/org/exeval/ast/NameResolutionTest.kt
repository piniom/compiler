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
    fun `should match variable to declarations with pointers`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   let pointer: [Int] = new [Int] (5);
        //   pointer[0];
        //   del pointer
        // }
        // ```
        val memoryNew = MemoryNew(ArrayType(IntType), listOf(PositionalArgument(IntLiteral(5))))
        val declaration = ConstantDeclaration("pointer", ArrayType(IntType),  memoryNew)
        val reference1 = VariableReference("pointer")
        val arrayAccess = ArrayAccess(reference1, IntLiteral(0))
        val reference2 = VariableReference("pointer")
        val memoryDel = MemoryDel(reference2)
        val function = FunctionDeclaration(
            "main", emptyList(), NopeType,
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

    @Test
    fun `should get shadowed variable`() {
        val outerDecl = ConstantDeclaration("var", IntType, IntLiteral(1))
        val innerDecl = MutableVariableDeclaration("var", IntType, IntLiteral(2))
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
            "main", listOf(Parameter("a", IntType), Parameter("a", IntType)),
            IntType,
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
                MutableVariableDeclaration("a", IntType, IntLiteral(1)),
                MutableVariableDeclaration("a", IntType, IntLiteral(2))
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
            "main", listOf(Parameter("a", IntType), Parameter("a", IntType)),
            IntType
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
        val decl = ForeignFunctionDeclaration("f", emptyList(), IntType)

        val program =
            Program(listOf(
                decl,
                FunctionDeclaration(
                    "main", emptyList(), IntType,
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

    @Test
    fun `should connect struct usages and declarations`(){
        /*
        struct A{}
        a: A
        */
        val decl = StructTypeDeclaration(
            "A",
            listOf(
                MutableVariableDeclaration("a", IntType),
                MutableVariableDeclaration("a", IntType)
            ),
            ConstructorDeclaration(listOf(),Block(listOf()))
        )
        val use = TypeUse("a")
        val ast = Block(listOf(
            decl,
            MutableVariableDeclaration("a",use)
        ))
        val result = NameResolutionGenerator(AstInfo(ast,mapOf())).parse()
        assert(result.result.typeNameToDecl[use]!==decl)
    }

    @Test
    fun `should handle constructor`(){
        val b_decl = MutableVariableDeclaration("b",IntType)
        val b_ref = VariableReference("b")
        val c_param = Parameter("c",IntType)
        val c_ref = VariableReference("c")
        val a_decl = MutableVariableDeclaration("a", IntType)
        val a_mem_ref = VariableReference("a")
        val a_ass = Assignment(a_mem_ref,b_ref)
        val decl = StructTypeDeclaration(
            "A",
            listOf(
                a_decl
            ),
            ConstructorDeclaration(listOf(c_param),Block(listOf(
                Assignment(StructFieldAccess(HereReference(),"a"), b_ref),
                Assignment(StructFieldAccess(HereReference(),"a"), c_ref),
                a_ass
            )))
        )
        val ast = Block(listOf(b_decl,decl))
        val result = NameResolutionGenerator(AstInfo(ast,mapOf())).parse()
        assert(result.diagnostics.size==0)
        assert(result.result.variableToDecl[b_ref]!! == b_decl)
        assert(result.result.variableToDecl[c_ref]!! == c_param)
    }

    @Test
    fun `should detect duplicates`(){
        /*
        struct A{
            int a;
            int a;
        }
        struct A{
            int a;
        }
        */
        val ast = Block(listOf(
            StructTypeDeclaration(
                "A",
                listOf(
                    MutableVariableDeclaration("a", IntType),
                    MutableVariableDeclaration("a", IntType)
                ),
                ConstructorDeclaration(listOf(),Block(listOf()))
            ),
            StructTypeDeclaration(
                "A",
                listOf(
                    MutableVariableDeclaration("a", IntType)
                ),
                ConstructorDeclaration(listOf(),Block(listOf()))
            )
        ))
        val result = NameResolutionGenerator(AstInfo(ast,mapOf())).parse()
        //expect 2 errors - duplicate member and definition
        assert(result.diagnostics.size == 2)
    }

}