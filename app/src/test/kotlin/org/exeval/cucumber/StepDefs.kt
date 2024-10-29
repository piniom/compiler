package org.exeval.cucumber

import io.cucumber.java.en.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.Assert.*

import org.exeval.buildLexer
import org.exeval.buildInput
import org.exeval.input.interfaces.Input
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult

private val logger = KotlinLogging.logger {}

class StepDefs {

    private lateinit var sourceCode: Input
    private lateinit var lexerOutput: OperationResult<List<LexerToken>>

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

    @Then("no errors are returned")
    fun ensureThereAreNoErrors() {
        try {
            for (diagnostic in lexerOutput.diagnostics) {
                logger.info{"unexpected diagnostic: ${diagnostic.message}"}
            }
            assertEquals(0, lexerOutput.diagnostics.size);
        } catch (e: UninitializedPropertyAccessException) {
            fail("Lexer output not known. Step starting lexer must be run first.")
        }
    }

}
