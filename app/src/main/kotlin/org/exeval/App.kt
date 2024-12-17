/*
 * This source file was generated by the Gradle 'init' task
 */
package org.exeval

import io.github.oshai.kotlinlogging.KotlinLogging
import org.exeval.ast.*
import kotlin.system.exitProcess
import java.io.FileNotFoundException

import org.exeval.automata.interfaces.DFA
import org.exeval.cfg.*
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.input.CommentCutter
import org.exeval.input.FileInput
import org.exeval.input.interfaces.Input
import org.exeval.instructions.InstructionCoverer
import org.exeval.instructions.InstructionSetCreator
import org.exeval.instructions.LivenessCheckerImpl
import org.exeval.instructions.RegisterAllocatorImpl
import org.exeval.instructions.interfaces.LivenessResult
import org.exeval.instructions.linearizer.BasicBlock
import org.exeval.instructions.linearizer.Linearizer
import org.exeval.lexer.DFAmin
import org.exeval.lexer.DFAParserImpl
import org.exeval.lexer.MultipleTokensLexer
import org.exeval.lexer.NFAParserImpl
import org.exeval.lexer.interfaces.Lexer
import org.exeval.lexer.interfaces.RegexParser
import org.exeval.lexer.regexparser.RegexParserImpl
import org.exeval.parser.Parser
import org.exeval.parser.grammar.GrammarSymbol
import org.exeval.parser.grammar.LanguageGrammar
import org.exeval.parser.utilities.GrammarAnalyser
import org.exeval.utilities.CodeBuilder
import org.exeval.utilities.TokenCategories
import org.exeval.utilities.LexerUtils
import org.exeval.utilities.interfaces.OperationResult
import java.io.File
import java.io.IOException

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        logger.error { "Input file not provided. Use `./gradlew run --args=\"<file name>\"'" }
        exitProcess(1)
    }

    // Input
    val sourceCode = buildInput(args[0])

    // Lexer
    val lexer = buildLexer()
    val lexerOutput = lexer.run(sourceCode)
    for (diagnostic in lexerOutput.diagnostics) {
        logger.warn { "[Lexer diagnostic] ${diagnostic.message}" }
    }

    // Parser
    val leaves = LexerUtils.lexerTokensToParseTreeLeaves(lexerOutput.result)
    val parseTree = buildParser().run(leaves)
    val astInfo = AstCreatorImpl().create(parseTree, sourceCode)

    // AST
    val nameResolutionOutput = NameResolutionGenerator(astInfo).parse()
    for (diagnostic in nameResolutionOutput.diagnostics) {
        logger.warn { "[NameResolution diagnostic] ${diagnostic.message}" }
    }

    val typeCheckerOutput = TypeChecker(astInfo, nameResolutionOutput.result).parse()
    for (diagnostic in typeCheckerOutput.diagnostics) {
        logger.warn { "[TypeChecker diagnostic] ${diagnostic.message}" }
    }

    val functionAnalisisResult = FunctionAnalyser().analyseFunctions(astInfo)

    val functions = (astInfo.root as Program).functions

    val frameManagers = mutableMapOf<FunctionDeclaration, FunctionFrameManager>()
    for (function in functions) {
        frameManagers[function] = FunctionFrameManagerImpl(function, functionAnalisisResult, frameManagers)
    }

    // CFG
    val nodes = functionNodes(functions, functionAnalisisResult, nameResolutionOutput, frameManagers, typeCheckerOutput)

    val codeBuilder = CodeBuilder(functionAnalisisResult.maxNestedFunctionDepth());

    // Linearization
    val instructionPatterns = InstructionSetCreator().createInstructionSet()
    val linearizer = Linearizer(InstructionCoverer(instructionPatterns))
    val linearizedFunctions = nodes.map { it.first to linearizer.createBasicBlocks(it.second) }.map {
        val domain = registersDomain(it.second)
        val livenessResult = LivenessCheckerImpl().check(it.second)
        val registerMapping = RegisterAllocatorImpl().allocate(livenessResult, domain, PhysicalRegister.range())
        codeBuilder.addFunction(it.first, it.second, registerMapping.mapping)
    }

    // External proccesses
    val asmFile = File("program.asm")
    asmFile.writeText(codeBuilder.code)
    logger.info { "Assembly code written to ${asmFile.absolutePath}" }

    val nasmProcess = ProcessBuilder("nasm", "-felf64", "program.asm", "-o", "program.o")
        .inheritIO()
        .start()
    nasmProcess.waitFor()
    if (nasmProcess.exitValue() != 0) {
        logger.error { "Couldn't run \"nasm\"!" }
        exitProcess(1)
    }
    logger.info { "NASM successfully created program.o" }

    val gccProcess = ProcessBuilder("gcc", "program.o", "-o", "program")
        .inheritIO()
        .start()
    gccProcess.waitFor()
    if (gccProcess.exitValue() != 0) {
        logger.error { "Couldn't run \"gcc\"!" }
        exitProcess(1)
    }

    logger.info { "Successfully linked using gcc" }
}

fun buildLexer(): Lexer {
    val tokens = TokenCategories.entries.toTypedArray()
    val regexParser = RegexParserImpl()
    val dfas = tokens.associateBy({ regexToDfa(regexParser, it.regex) }, { it })
    val lexer = MultipleTokensLexer(dfas)
    return lexer
}

private fun regexToDfa(regexParser: RegexParser, regexString: String): DFA<*> {
    val regex = regexParser.parse(regexString)
    val nfaParser = NFAParserImpl({ it })

    val nfa = nfaParser.parse(regex)
    val dfaParser = DFAParserImpl<Any>()
    @Suppress("UNCHECKED_CAST") val dfa = dfaParser.parse(nfa) as DFA<Any>
    val minimizer = DFAmin<Any>()
    val dfaMin = minimizer.minimize(dfa)
    return dfaMin
}

fun buildInput(fileName: String): Input {
    try {
        return CommentCutter(FileInput(fileName))
    } catch (e: FileNotFoundException) {
        logger.error { "Input file `${fileName}' does not exist" }
        exitProcess(1)
    }
}

fun buildParser(): Parser<GrammarSymbol> {
    val grammarAnalyser = GrammarAnalyser()
    val analyzedGrammar = grammarAnalyser.analyseGrammar(LanguageGrammar.grammar)
    val parser = Parser(analyzedGrammar)
    return parser
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
    typeCheckerOutput: OperationResult<TypeMap>
) = functions.map {
    val variableUsage =
        usageAnalysis(functionAnalisisResult.callGraph, nameResolutionOutput.result, it.body).getAnalysisResult()
    it.name to CFGMaker(
        frameManagers[it]!!,
        nameResolutionOutput.result,
        variableUsage,
        typeCheckerOutput.result
    ).makeCfg(it)
}
