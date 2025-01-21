package org.exeval.cucumber

import io.cucumber.java.DataTableType
import io.cucumber.java.en.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.exeval.*
import org.exeval.ast.*
import org.exeval.cfg.CFGMaker
import org.junit.Assert.*

import org.exeval.cfg.ForeignCallManager
import org.exeval.cfg.FunctionFrameManagerImpl
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.ffm.interfaces.CallManager
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.input.interfaces.Input
import org.exeval.instructions.InstructionCoverer
import org.exeval.instructions.InstructionSetCreator
import org.exeval.instructions.LivenessCheckerImpl
import org.exeval.instructions.RegisterAllocatorImpl
import org.exeval.instructions.linearizer.BasicBlock
import org.exeval.instructions.linearizer.Linearizer
import org.exeval.parser.grammar.PrecompiledParserFactory
import org.exeval.parser.parser.ParseError
import org.exeval.parser.parser.Parser
import org.exeval.parser.grammar.GrammarSymbol
import org.exeval.parser.grammar.LanguageGrammar
import org.exeval.parser.utilities.GrammarAnalyser
import org.exeval.utilities.CodeBuilder
import org.exeval.utilities.LexerUtils
import org.exeval.utilities.SimpleDiagnostics
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult
import org.exeval.utilities.TokenCategories
import org.exeval.utilities.interfaces.Diagnostics

private val logger = KotlinLogging.logger {}

class StepDefs {
    private lateinit var sourceCode: Input
    private lateinit var lexerOutput: OperationResult<List<LexerToken>>
    private lateinit var parserDiagnostic: Diagnostics
    private lateinit var astInfo: AstInfo
    private lateinit var nameResolutionOutput: OperationResult<NameResolution>
    private lateinit var typeCheckerOutput: OperationResult<TypeMap>
    private lateinit var constCheckerOutput: List<Diagnostics>
    private lateinit var outAsm: String

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
        prepareAndRunLexer()

        if (!::lexerOutput.isInitialized || lexerOutput.diagnostics.isNotEmpty()) {
            return
        }

        try {
            val leaves = LexerUtils.lexerTokensToParseTreeLeaves(lexerOutput.result)
            val output = buildParser().run(leaves)
            astInfo = AstCreatorImpl().create(output, sourceCode)
        } catch (e: ParseError) {
            parserDiagnostic = SimpleDiagnostics(e.message, e.startErrorLocation, e.endErrorLocation)
        }

    }

    @When("source code is passed through name resolution")
    fun prepareAndRunNameResolution() {
        prepareAndRunLexerAndParser()
        if (!::astInfo.isInitialized || ::parserDiagnostic.isInitialized ) {
            return
        }

        nameResolutionOutput = NameResolutionGenerator(astInfo).parse()
    }

    @When("source code is passed through type checker")
    fun prepareAndRunTypeChecker() {
        if (!::constCheckerOutput.isInitialized || nameResolutionOutput.diagnostics.isNotEmpty() || constCheckerOutput.isNotEmpty()) {
            return
        }

        typeCheckerOutput = TypeChecker(astInfo, nameResolutionOutput.result).parse()
    }

    @When("source code is compiled to asm")
    fun prepareAndRunCodeGenerator() {
        prepareAndRunTypeChecker()
        if (!::typeCheckerOutput.isInitialized || typeCheckerOutput.diagnostics.isNotEmpty()) {
            return
        }

        generateCode()
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
        if (::parserDiagnostic.isInitialized) {
            result.add(parserDiagnostic)
        }
        if (::nameResolutionOutput.isInitialized) {
            result.addAll(nameResolutionOutput.diagnostics)
        }
        if (::constCheckerOutput.isInitialized) {
            result.addAll(constCheckerOutput)
        }
        if (::typeCheckerOutput.isInitialized) {
            result.addAll(typeCheckerOutput.diagnostics)
        }

        return result
    }

    private fun buildParser(): Parser<GrammarSymbol> {
        val grammarAnalyser = GrammarAnalyser()
        val analyzedGrammar = grammarAnalyser.analyseGrammar(LanguageGrammar.grammar)
        val parser = PrecompiledParserFactory().create(analyzedGrammar)
        return parser
    }

    private fun generateCode()
    {
        try {
            val functionAnalisisResult = FunctionAnalyser().analyseFunctions(astInfo)

            val functions = (astInfo.root as Program).functions.filterIsInstance<FunctionDeclaration>()

            val frameManagers = mutableMapOf<FunctionDeclaration, FunctionFrameManager>()
            for (function in functions) {
                frameManagers[function] = FunctionFrameManagerImpl(function, functionAnalisisResult, frameManagers)
            }

            val foreignFs = (astInfo.root as Program).functions.filterIsInstance<ForeignFunctionDeclaration>()
            val fCallMMap = foreignFs.associate { it to ForeignCallManager(it) }

            val nodes = functionNodes(
                functions,
                functionAnalisisResult,
                nameResolutionOutput,
                frameManagers,
                typeCheckerOutput,
                fCallMMap
            )

            val codeBuilder = CodeBuilder(functionAnalisisResult.maxNestedFunctionDepth());

            val instructionPatterns = InstructionSetCreator().createInstructionSet()
            val linearizer = Linearizer(InstructionCoverer(instructionPatterns))
            val linearizedFunctions = nodes.map { it.first to linearizer.createBasicBlocks(it.second) }.map {
                val domain = registersDomain(it.second)
                val livenessResult = LivenessCheckerImpl().check(it.second)
                val registerMapping = RegisterAllocatorImpl().allocate(livenessResult, domain, PhysicalRegister.range())
                codeBuilder.addFunction(it.first, it.second, registerMapping.mapping)
            }

            outAsm = codeBuilder.code
        } catch (e: Exception) {
            fail("Failed to generate code: ${e.message}")
        }
    }


    private fun registersDomain(linearizedFunction: List<BasicBlock>) =
        linearizedFunction.map { basicBlock ->
            basicBlock.instructions.map { instruction ->
                listOf(
                    instruction.definedRegisters(),
                    instruction.usedRegisters()
                )
            }
        }.flatten().flatten().flatten().distinct().toSet()

    private fun functionNodes(
        functions: List<FunctionDeclaration>,
        functionAnalisisResult: FunctionAnalysisResult,
        nameResolutionOutput: OperationResult<NameResolution>,
        frameManagers: MutableMap<FunctionDeclaration, FunctionFrameManager>,
        typeCheckerOutput: OperationResult<TypeMap>,
        foreignCallManagers: Map<ForeignFunctionDeclaration, CallManager>
    ) = functions.map {
        val variableUsage =
            usageAnalysis(functionAnalisisResult.callGraph, nameResolutionOutput.result, it.body).run()
        it.name to CFGMaker(
            frameManagers[it]!!,
            nameResolutionOutput.result,
            variableUsage,
            typeCheckerOutput.result,
            frameManagers + foreignCallManagers
        ).makeCfg(it)
    }

}
