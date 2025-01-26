import org.exeval.ast.*
import org.exeval.ast.IntTypeNode
import org.exeval.input.StringInput
import org.exeval.input.interfaces.Input
import org.exeval.parser.Production
import org.exeval.parser.grammar.*
import org.exeval.parser.interfaces.ParseTree
import org.junit.Ignore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.exeval.utilities.TokenCategories as Token

private typealias Br = ParseTree.Branch<GrammarSymbol>
private typealias Lf = ParseTree.Leaf<GrammarSymbol>
private typealias Pr = ParseTree.Leaf<GrammarSymbol>

class AstCreatorImplTest {

    private val astCreator = AstCreatorImpl()

    @Ignore("There is bug in StringInput, should unignore after solving issue #197")
    fun `Create should parse a program containing a single main function which returns a constant value`() {
        val codeStr = """foo main() -> Int = 4"""
        val strInput: Input = StringInput(codeStr)
        val locations = (0..codeStr.length).map {
            val location = strInput.location
            strInput.nextChar()
            location
        }
        strInput.location = locations[0]

        val programBranch = Br(
            Production<GrammarSymbol>(
                ProgramSymbol, listOf(TopLevelStatementsDeclarationsSymbol)
            ), listOf(
                Br(
                    Production<GrammarSymbol>(
                        TopLevelStatementsDeclarationsSymbol, listOf(FunctionDeclarationSymbol)
                    ), listOf(
                        Br(
                            Production<GrammarSymbol>(
                                FunctionDeclarationSymbol, listOf(
                                    Token.KeywordFoo,
                                    Token.IdentifierEntrypoint,
                                    Token.LiteralNope,
                                    Token.PunctuationArrow,
                                    Token.IdentifierType,
                                    Token.OperatorAssign,
                                    ExpressionSymbol,
                                )
                            ), listOf(
                                Lf(Token.KeywordFoo, locations.first(), locations[3]),
                                Lf(Token.IdentifierEntrypoint, locations[4], locations[8]),
                                Lf(Token.LiteralNope, locations[8], locations[10]),
                                Lf(Token.PunctuationArrow, locations[11], locations[13]),
                                Lf(Token.IdentifierType, locations[14], locations[17]),
                                Lf(Token.OperatorAssign, locations[18], locations[19]),
                                Br(
                                    Production(
                                        ExpressionSymbol,
                                        listOf(SimpleExpressionSymbol),
                                    ), listOf(
                                        Br(
                                            Production(
                                                SimpleExpressionSymbol, listOf(ValueSymbol)
                                            ), listOf(
                                                Lf(Token.LiteralInteger, locations[20], locations[21])
                                            ), locations[20], locations[21]
                                        )
                                    ), locations[20], locations[21]
                                )
                            ), locations.first(), locations.last()
                        )
                    ), locations.first(), locations.last()
                )
            ), locations.first(), locations.last()
        )

        // Act
        val result = astCreator.create(programBranch, strInput)

        // Assert
        assertNotNull(result)
        assertTrue(result.root is Program)
        val programNode = result.root as Program
        assertEquals(1, programNode.functions.size)

        val functionNode = programNode.functions.first()

        assertEquals("main", functionNode.name)
        assertEquals(0, functionNode.parameters.size)
        assertTrue(functionNode.returnType is IntTypeNode)

        assertTrue(functionNode is FunctionDeclaration)
        if (functionNode is FunctionDeclaration) {
            assertTrue(functionNode.body is IntLiteral)
            assertEquals(4, (functionNode.body as IntLiteral).value)
        }
    }
}
