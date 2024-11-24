import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.exeval.ast.*
import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location
import org.exeval.parser.grammar.*
import org.exeval.parser.interfaces.ParseTree
import org.exeval.utilities.LocationRange
import org.exeval.utilities.TokenCategories
import org.junit.jupiter.api.assertThrows

class AstCreatorImplTest {

    private val astCreator = AstCreatorImpl()

    @Test
    fun `create should parse a program containing a single main function with a constant return value`() {
        // Arrange
        val inputMock = mockk<Input>(relaxed = true)

        // Mock start and end locations
        val startLocationMock = mockk<Location> {
            every { line } returns 1
            every { idx } returns 0
        }
        val endLocationMock = mockk<Location> {
            every { line } returns 1
            every { idx } returns 5
        }

        // Tokens for "foo main() -> Int = 4"
        val literalValueLeaf = mockk<ParseTree.Leaf<GrammarSymbol>> {
            every { symbol } returns TokenCategories.LiteralInteger
            every { startLocation } returns startLocationMock
            every { endLocation } returns endLocationMock
        }
        val valueBranch = mockk<ParseTree.Branch<GrammarSymbol>> {
            every { production.left } returns ValueSymbol
            every { children } returns listOf(literalValueLeaf)
            every { startLocation } returns startLocationMock
            every { endLocation } returns endLocationMock
        }
        val simpleExpressionBranch = mockk<ParseTree.Branch<GrammarSymbol>> {
            every { production.left } returns SimpleExpressionSymbol
            every { children } returns listOf(valueBranch)
            every { startLocation } returns startLocationMock
            every { endLocation } returns endLocationMock
        }
        val expressionBranch = mockk<ParseTree.Branch<GrammarSymbol>> {
            every { production.left } returns ExpressionSymbol
            every { children } returns listOf(simpleExpressionBranch)
            every { startLocation } returns startLocationMock
            every { endLocation } returns endLocationMock
        }
        val functionDeclarationBranch = mockk<ParseTree.Branch<GrammarSymbol>> {
            every { production.left } returns FunctionDeclarationSymbol
            every { children } returns listOf(
                mockk<ParseTree.Leaf<GrammarSymbol>> { every { symbol } returns TokenCategories.KeywordFoo },
                mockk<ParseTree.Leaf<GrammarSymbol>> { every { symbol } returns TokenCategories.IdentifierEntrypoint },
                mockk<ParseTree.Leaf<GrammarSymbol>> { every { symbol } returns TokenCategories.LiteralNope },
                mockk<ParseTree.Leaf<GrammarSymbol>> { every { symbol } returns TokenCategories.PunctuationArrow },
                mockk<ParseTree.Leaf<GrammarSymbol>> { every { symbol } returns TokenCategories.IdentifierType },
                mockk<ParseTree.Leaf<GrammarSymbol>> { every { symbol } returns TokenCategories.OperatorAssign },
                expressionBranch
            )
            every { startLocation } returns startLocationMock
            every { endLocation } returns endLocationMock
        }
        val functionsDeclarationsBranch = mockk<ParseTree.Branch<GrammarSymbol>> {
            every { production.left } returns FunctionsDeclarationsSymbol
            every { children } returns listOf(functionDeclarationBranch)
            every { startLocation } returns startLocationMock
            every { endLocation } returns endLocationMock
        }
        val programBranch = mockk<ParseTree.Branch<GrammarSymbol>> {
            every { production.left } returns ProgramSymbol
            every { children } returns listOf(functionsDeclarationsBranch)
            every { startLocation } returns startLocationMock
            every { endLocation } returns endLocationMock
        }

        every { inputMock.nextChar() } returnsMany "main".toCharArray().map { it } // Mock reading input

        // Act
        val result = astCreator.create(programBranch, inputMock)

        // Assert
        assertNotNull(result)
        assertTrue(result.root is Program)
        val programNode = result.root as Program
        assertEquals(1, programNode.functions.size)

        val functionNode = programNode.functions.first()
        assertEquals("main", functionNode.name)
        assertEquals(0, functionNode.parameters.size)
        assertTrue(functionNode.returnType is IntType)
        assertTrue(functionNode.body is IntLiteral)
        assertEquals(4, (functionNode.body as IntLiteral).value)
    }
}
