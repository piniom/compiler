package org.example.automata

interface IRegexConcat : IRegex {
    val expressions: List<IRegex>
}