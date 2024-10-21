package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.input.interfaces.Input
import org.exeval.lexer.interfaces.Lexer
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult
import org.exeval.utilities.interfaces.TokenCategory

class MultipleTokensLexer(private val dfas: Map<DFA<*>, TokenCategory>) : Lexer {
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
