package org.example.automata

interface IRegexUnion : IRegex {
    val expressions: List<IRegex>
}