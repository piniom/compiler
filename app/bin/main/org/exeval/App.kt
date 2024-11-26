/*
 * This source file was generated by the Gradle 'init' task
 */
package org.exeval

import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.exeval.utilities.TokenCategories

private val logger = KotlinLogging.logger {}

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }

    fun constructorsUsage() {
        val regexParser = RegexParserImpl()
    }
}

fun buildLexer(): Lexer {
    val tokens = TokenCategories.values()
    val regexParser = RegexParserImpl()
    val dfas = tokens.associateBy({ regexToDfa(regexParser, it.regex) } , { it })
    val lexer = MultipleTokensLexer(dfas)
    return lexer
}

private fun regexToDfa(regexParser: RegexParser, regexString: String): DFA<*> {
    val regex = regexParser.parse(regexString)
    val nfaParser = NFAParserImpl({ it })

    val nfa = nfaParser.parse(regex)
    val dfaParser = DFAParserImpl<Any>()
    val dfa = dfaParser.parse(nfa) as DFA<Any>
    val minimizer = DFAmin<Any>()
    val dfaMin = minimizer.minimize(dfa)
    return dfaMin
}

fun buildInput(fileName: String): Input {
    try {
        return CommentCutter(FileInput(fileName))
    } catch (e: FileNotFoundException) {
        logger.error{"Input file `${fileName}' does not exist"}
        exitProcess(1)
    }
}

fun main(args: Array<String>) {
    if (args.size == 0) {
        logger.error{"Input file not provided. Use `./gradlew run --args=\"<file name>\"'"}
        exitProcess(1)
    }
    val sourceCode = buildInput(args[0])
    val lexer = buildLexer()
    val lexerOutput = lexer.run(sourceCode)
    for (diagnostic in lexerOutput.diagnostics) {
        logger.warn{"[Lexer diagnostic] ${diagnostic.message}"}
    }
}
