package org.exeval.cucumber

import io.cucumber.java.DataTableType
import io.cucumber.java.en.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.exeval.buildInput
import org.exeval.buildLexer
import org.exeval.input.interfaces.Input
import org.exeval.utilities.TokenCategories
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult
import org.junit.Assert.*

private val logger = KotlinLogging.logger {}

class StepDefs {
	private lateinit var sourceCode: Input
	private lateinit var lexerOutput: OperationResult<List<LexerToken>>

	@Given("ExEval source code file {string}")
	fun readSourceCodeFile(fileName: String) {
		sourceCode = buildInput("src/test/resources/programs/$fileName")
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

	@Then("no errors are returned")
	fun ensureThereAreNoErrors() {
		try {
			for (diagnostic in lexerOutput.diagnostics) {
				logger.info { "unexpected diagnostic: ${diagnostic.message}" }
			}
			assertEquals(0, lexerOutput.diagnostics.size)
		} catch (e: UninitializedPropertyAccessException) {
			fail("Lexer output not known. Step starting lexer must be run first.")
		}
	}

	@Then("returned token list matches")
	fun verifyReturnedTokenList(expectedTokens: List<ExpectedToken>) {
		// Ignore whitespaces for test definition clarity
		val returnedTokens =
			lexerOutput.result.filter {
				it.categories.size != 1 ||
					!it.categories.contains(TokenCategories.Whitespace)
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
				it.split(", ").map { TokenCategories.valueOf(it) }.toSet()
			} ?: emptySet(),
		)
	}
}
