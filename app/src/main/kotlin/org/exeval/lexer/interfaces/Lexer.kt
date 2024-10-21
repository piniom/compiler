package org.exeval.lexer.interfaces

import org.exeval.automata.DFAWalker
import org.exeval.automata.interfaces.DFA
import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location
import org.exeval.utilities.SimpleDiagnostics
import org.exeval.utilities.SimpleLexerToken
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult
import org.exeval.utilities.interfaces.TokenCategory
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.collections.listOf

interface Lexer {
    fun run(input: Input): OperationResult<List<LexerToken>>
}

class ExevalLexer(private val dfas: Map<DFA<*>, TokenCategory>) : Lexer {
    override fun run(input: Input): OperationResult<List<LexerToken>> {
        val tokens = mutableListOf<LexerToken>()
        val diagnostics = mutableListOf<Diagnostics>()
        while (input.hasNextChar()) {
            val res = SingleTokenLexer(this.dfas, input).run()
            if (res.result != null) tokens.addLast(res.result)
            diagnostics.addAll(res.diagnostics)
        }
        return OperationResult(
            result = tokens,
            diagnostics = diagnostics
        )
    }
}

class SingleTokenLexer(dfas: Map<DFA<*>, TokenCategory>, input: Input) {
    private val activeWalkers: MutableMap<DFAWalker<*>, TokenCategory> =
        dfas.mapKeys { (dfa, _) -> DFAWalker(input.location, dfa) }.toMutableMap()
    private var accepted: MutableMap<DFAWalker<*>, TokenCategory>
    private val input: Input
    private val start: Location

    private val text: StringBuilder = StringBuilder()

    init {
        accepted = mutableMapOf()
        this.input = input
        this.start = input.location
    }

    public fun run(): OperationResult<LexerToken?> {
        while (activeWalkers.isNotEmpty()) {
            val char = this.input.nextChar() ?: break;
            for ((walker, category) in activeWalkers) {
                if (!walker.transition(char, input.location) || walker.isDead()) this.deactivateWalker(walker)
            }
            this.text.append(char)
        }
        val (walker, _) = accepted.entries.firstOrNull() ?: return OperationResult(
            null,
            listOf(SimpleDiagnostics("String \"$text\" didn't match any tokens!", this.start, this.input.location))
        )
        val acceptedLoc = walker.maxAccepting!!
        if (acceptedLoc != this.input.location) {
            this.input.location = acceptedLoc
            this.input.nextChar()
        }
        return OperationResult(
            SimpleLexerToken(
                accepted.values.toSet(),
                text.substring(0, walker.maxAcceptingCount),
                this.start,
                acceptedLoc
            ),
            listOf()
        )
    }

    private fun deactivateWalker(walker: DFAWalker<*>) {
        val category = this.activeWalkers.remove(walker) ?: throw Exception("Walker not present!")
        if (walker.maxAccepting == null) return
        val any = accepted.entries.firstOrNull()
        if (any == null || walker.compareTo(any.key) == 0) {
            accepted.put(walker, category)
            return
        }
        if (walker.compareTo(any.key) < 0) return
        this.accepted = mutableMapOf(walker to category)
    }
}