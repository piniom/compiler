package org.exeval.cucumber

import io.cucumber.java.DataTableType
import io.cucumber.java.en.*
import io.cucumber.messages.types.DataTable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.InternalPlatformDsl.toArray
import org.junit.Assert.*

import org.exeval.buildLexer
import org.exeval.buildInput
import org.exeval.input.interfaces.Input
import org.exeval.parser.grammar.GrammarSymbol
import org.exeval.parser.interfaces.ParseTree
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult
import org.exeval.utilities.TokenCategories
import org.exeval.utilities.interfaces.Diagnostics

private val logger = KotlinLogging.logger {}

class StepDefs {

    private lateinit var sourceCode: Input
    private lateinit var lexerOutput: OperationResult<List<LexerToken>>
    private lateinit var parserOutput: OperationResult<ParseTree<GrammarSymbol>>

    @Given("ExEval source code file {string}")
    fun readSourceCodeFile(fileName: String) {
        sourceCode = buildInput("src/test/resources/programs/${fileName}")
    }

    @When("source code is passed through lexer")
    fun prepareAndRunLexer() {
        val lexer = buildLexer()
        try {
            lexerOutput = lexer.run(sourceCode)
        } catch (e: UninitializedPropertyAccessException) {
            fail("Input not known. Step providing source code must be run first.")
        }
    }

    @When("source code is passed through parser")
    fun prepareAndRunLexerAndParser() {
        val lexer = buildLexer()
        try {
            lexerOutput = lexer.run(sourceCode)
        } catch (e: UninitializedPropertyAccessException) {
            fail("Input not known. Step providing source code must be run first.")
        }
        // TODO: Implement parser
    }

    @Then("no errors are returned")
    fun ensureThereAreNoErrors() {
        val allDiagnostics = getAllDiagnostics()
        for (diagnostic in allDiagnostics) {
            logger.info{"unexpected diagnostic: ${diagnostic.message}"}
        }
        assertEquals(0, allDiagnostics.size)
    }

    @Then("returns diagnostics:")
    fun checkMultipleDiagnostics(expected: List<ExpectedDiagnostic>) {
        val diagnostics = getAllDiagnostics()
        assertEquals(expected.size, diagnostics.size)

        expected.zip(diagnostics).forEach { (expected, actual) ->
            checkDiagnostic(actual, expected.message, expected.line, expected.column, expected.endLine, expected.endColumn)
        }
    }

    @Then("returns diagnostic with message {string} that starts at line {int} and column {int} and ends at line {int} and column {int}")
    fun verifySingleDiagnostic(message: String, line: Int, column: Int, endLine: Int, endColumn: Int) {
        try {
            val diagnostics = getAllDiagnostics()
            assertEquals(1, diagnostics.size)
            val diagnostic = diagnostics[0]
            checkDiagnostic(diagnostic, message, line, column, endLine, endColumn)

        } catch (e: UninitializedPropertyAccessException) {
            fail("Lexer output not known. Step starting lexer must be run first.")
        }
    }

	@Then("returned token list matches")
	fun verifyReturnedTokenList(expectedTokens: List<ExpectedToken>) {
		// Ignore whitespaces for test definition clarity
		val returnedTokens = lexerOutput.result.filter {
			it.categories.size != 1
			|| !it.categories.contains(TokenCategories.Whitespace)
		}
		assertEquals(expectedTokens.size, returnedTokens.size)

		expectedTokens.zip(returnedTokens).forEach { pair ->
			val expected = pair.component1()
			val returned = pair.component2()
			assertEquals(expected.text, returned.text)
			assertEquals(expected.categories, returned.categories)
		}
	}

	data class ExpectedToken(val text: String, val categories: Set<TokenCategories>)

	@DataTableType
	fun expectedTokenEntry(entry: Map<String, String>): ExpectedToken {
		return ExpectedToken(
			entry["text"] ?: "",
			entry["categories"]?.let {
				it.split(", ").map{ TokenCategories.valueOf(it) }.toSet()
			} ?: emptySet()
		)
	}

    data class ExpectedDiagnostic(val message: String, val line: Int, val column: Int, val endLine: Int, val endColumn: Int)

    @DataTableType
    fun expectedDiagnosticEntry(entry: Map<String, String>): ExpectedDiagnostic {
        return ExpectedDiagnostic(
            message = entry["message"] ?: error("Missing 'message' field"),
            line = entry["line"]?.toIntOrNull() ?: error("Invalid or missing 'line' field"),
            column = entry["column"]?.toIntOrNull() ?: error("Invalid or missing 'column' field"),
            endLine = entry["endLine"]?.toIntOrNull() ?: error("Invalid or missing 'endLine' field"),
            endColumn = entry["endColumn"]?.toIntOrNull() ?: error("Invalid or missing 'endColumn' field")
        )
    }

    private fun checkDiagnostic(actual: Diagnostics, message: String, line: Int, column: Int, endLine: Int, endColumn: Int) {
        assertEquals(message, actual.message)
        assertEquals(line, actual.startLocation.line)
        assertEquals(column, actual.startLocation.idx)
        assertEquals(endLine, actual.stopLocation.line)
        assertEquals(endColumn, actual.stopLocation.idx)
    }

    private fun getAllDiagnostics(): List<Diagnostics> {
        val result = mutableListOf<Diagnostics>()
        if (::lexerOutput.isInitialized) {
            result.addAll(lexerOutput.diagnostics)
        }
        if (::parserOutput.isInitialized) {
            result.addAll(parserOutput.diagnostics)
        }
        return result
    }

}
