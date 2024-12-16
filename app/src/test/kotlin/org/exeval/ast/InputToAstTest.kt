package org.exeval.ast

import org.exeval.ast.valid.FilesToAst
import org.exeval.buildLexer
import org.exeval.buildParser
import org.exeval.input.CommentCutter
import org.exeval.input.FileInput
import org.exeval.input.StringInput
import org.exeval.input.interfaces.Input
import org.exeval.utilities.LexerUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Ignore
class InputToAstTest {

    private val astCreator = AstCreatorImpl()
    private val lexer = buildLexer()
    private val parser = buildParser()

    @ParameterizedTest
    @MethodSource("filenameParameters")
    fun `Valid ASTs are produced from example programs`(filepath: String) {
        val expectedAst = FilesToAst.MAP[filepath]!!
        testFileToAst(filepath, expectedAst)
    }


    private fun testFileToAst(filename: String, expectedAst: ASTNode) {
        val fileInput = CommentCutter(FileInput(filename))
        val actualAst = getActualAst(fileInput).root
        assertTrue(
            AstComparator.compareASTNodes(expectedAst, actualAst),
            "Wrong AST for $filename:\n\nactual:\n$actualAst\n\nexpected:\n$expectedAst"
        )
    }

    @Test
    fun `Test from StringInput main = 4 to AST`() {
        val codeStr = """foo main() -> Int = 4"""
        val stringInput = CommentCutter(StringInput(codeStr))
        val actualAst = getActualAst(stringInput)

        assertNotNull(actualAst)
        assertTrue(actualAst.root is Program)
        val programNode = actualAst.root
        assertEquals(1, programNode.functions.size)

        val functionNode = programNode.functions.first()
        assertEquals("main", functionNode.name)
        assertEquals(0, functionNode.parameters.size)
        assertTrue(functionNode.returnType is IntType)

        if (functionNode is FunctionDeclaration) {
            assertTrue(functionNode.body is IntLiteral)
            assertEquals(4, (functionNode.body).value)
        }
    }

    private fun getActualAst(input: Input): AstInfo {
        val lexerOutput = lexer.run(input)
        val leaves = LexerUtils.Companion.lexerTokensToParseTreeLeaves(lexerOutput.result)
        val parseTree = parser.run(leaves)
        val actualAst = astCreator.create(parseTree, input)
        return actualAst
    }

    companion object {
        @JvmStatic
        private fun filenameParameters(): List<String> = FilesToAst.MAP.keys.toList()
    }
}