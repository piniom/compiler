/*
 * This source file was generated by the Gradle 'init' task
 */
package org.exeval

import io.github.oshai.kotlinlogging.KotlinLogging
import org.exeval.ast.AstCreatorImpl
import kotlin.system.exitProcess
import java.io.FileNotFoundException

import org.exeval.automata.interfaces.DFA
import org.exeval.input.CommentCutter
import org.exeval.input.FileInput
import org.exeval.input.interfaces.Input
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
import org.exeval.utilities.TokenCategories
import org.exeval.utilities.LexerUtils

private val logger = KotlinLogging.logger {}

fun buildLexer(): Lexer {
    val tokens = TokenCategories.values()
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

fun main(args: Array<String>) {
    if (args.size == 0) {
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
    val parser = buildParser()
    val parseTree = parser.run(leaves)
    val astCreator = AstCreatorImpl()
    val ast = astCreator.create(parseTree, sourceCode)
}
