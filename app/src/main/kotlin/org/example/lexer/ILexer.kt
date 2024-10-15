package org.example.lexer

import org.example.input.interfaces.IInput
import org.example.utilities.IDiagnostics
import org.example.utilities.ILexerToken

interface ILexer {
    fun run(input: IInput): Pair<List<ILexerToken>, List<IDiagnostics>>
}