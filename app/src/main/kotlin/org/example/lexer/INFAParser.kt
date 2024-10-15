package org.example.lexer

import org.example.automata.IRegex
import org.example.automata.INFA

interface INFAParser<S> {
    fun parse(regex: IRegex): INFA<S>
}