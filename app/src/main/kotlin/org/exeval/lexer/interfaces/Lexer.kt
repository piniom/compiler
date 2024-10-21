package org.exeval.lexer.interfaces

import org.exeval.input.interfaces.Input
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult

interface Lexer {
    fun run(input: Input): OperationResult<List<LexerToken>>
}

