package org.example.lexer

import org.example.automata.IRegex

interface IRegexParser {
    fun parse(pattern: String): IRegex
}