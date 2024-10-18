package org.exeval.cucumber

import io.cucumber.java.en.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.Assert.*

import org.exeval.input.interfaces.Input
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult

private val logger = KotlinLogging.logger {}

class StepDefs {

    private lateinit var sourceCode: Input
    private lateinit var lexerOutput: OperationResult<List<LexerToken>>

    @Given("ExEval source code file {string}")
    fun readSourceCodeFile(fileName: String) {
        logger.error { "FileInput implementation needed" }
    }

    @When("source code is passed through lexer")
    fun prepareAndRunLexer() {
        /*
        val lexer: Lexer = //???
        try {
            lexerOutput = lexer.run(input)
        } catch (e: UninitializedPropertyAccessException) {
            //fail("Input not known. Step providing source code must be run first.")
            logger.error { "FileInput implementation needed" }
        }
        */
        logger.error { "Lexer implementation needed" }
    }

    @Then("no errors are returned")
    fun ensureThereAreNoErrors() {
        try {
            assertEquals(lexerOutput.diagnostics.size, 0);
        } catch (e: UninitializedPropertyAccessException) {
            //fail("Lexer output not known. Step starting lexer must be run first.")
            logger.error { "Lexer implementation needed" }
        }
    }

}
