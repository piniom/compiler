package org.exeval.lexer.regexparser

import org.exeval.automata.interfaces.Regex
import org.exeval.lexer.interfaces.BadRegexFormatException


class TokensToRegex {
    private var tokens: List<RegexToken> = listOf()

    fun convert(tokens: List<RegexToken>): Regex {
        this.tokens = tokens
        val nestedRes = recursiveConverter(0, this.tokens.size - 1)
        this.tokens = listOf()
        val res = flattenTreeForUnions(flattenTreeForConcats(nestedRes))

        return res
    }

    private fun recursiveConverter(first: Int, last: Int): Regex {
        val edgeCasesRegex = processEdgeCases(first, last)
        if (edgeCasesRegex != null) return edgeCasesRegex

        val lastOperators = findLastOperators(first, last)
        return processLastOperators(lastOperators, first, last)
    }

    private fun processEdgeCases(first: Int, last: Int): Regex? {
        if (first > last) return Regex.Union(setOf())
        if (first == last) {
            when (val cur = tokens[first]) {
                is RegexToken.Atom -> return Regex.Atom(cur.char)
                is RegexToken.Group -> {
                    val charList = cur.group.map { c: Char -> Regex.Atom(c) }
                    return Regex.Union(charList.toSet())
                }

                else -> {
                    throw BadRegexFormatException(SOMETHING_WRONG_ERROR.format(first))
                }
            }
        }

        return null
    }

    private fun findLastOperators(first: Int, last: Int): LastOperators {
        val lastOperators = LastOperators()

        var i = first
        while (i < last) {
            val cur = tokens[i]
            when (cur) {
                RegexToken.OpeningBracket -> {
                    i = findClosingBracket(i)
                    continue
                }

                RegexToken.ClosingBracket, is RegexToken.Group, is RegexToken.Atom -> {
                    if (checkForConcat(i)) lastOperators.lastConcatAfter = i
                }

                RegexToken.Star -> {
                    lastOperators.lastStar = i
                    if (checkForConcat(i)) lastOperators.lastConcatAfter = i
                }

                RegexToken.Union -> lastOperators.lastUnion = i
            }

            i++
        }

        val lastToken = tokens[last]
        when (lastToken) {
            is RegexToken.Star -> lastOperators.lastStar = last
            else -> {}
        }

        return lastOperators
    }

    private fun processLastOperators(lastOperators: LastOperators, first: Int, last: Int): Regex {
        val lastUnion = lastOperators.lastUnion
        val lastConcatAfter = lastOperators.lastConcatAfter
        val lastStar = lastOperators.lastStar

        if (lastUnion != null) {
            val firstRegex = recursiveConverter(first, lastUnion - 1)
            val secondRegex = recursiveConverter(lastUnion + 1, last)
            return Regex.Union(setOf(firstRegex, secondRegex))
        } else if (lastConcatAfter != null) {
            val firstRegex = recursiveConverter(first, lastConcatAfter)
            val secondRegex = recursiveConverter(lastConcatAfter + 1, last)
            return Regex.Concat(listOf(firstRegex, secondRegex))
        } else if (lastStar != null) {
            val regex = Regex.Star(recursiveConverter(first, lastStar - 1))
            return regex
        }

        return recursiveConverter(first + 1, last - 1)
    }

    private fun findClosingBracket(bracketIndex: Int): Int {
        var counter = 0
        var i = bracketIndex
        do {
            val cur = tokens[i]
            when (cur) {
                is RegexToken.OpeningBracket -> counter++
                is RegexToken.ClosingBracket -> counter--
                else -> {}
            }
            i++
        } while (counter > 0)

        return i - 1
    }

    private fun checkForConcat(index: Int): Boolean {
        val next = tokens[index + 1]
        return next is RegexToken.OpeningBracket || next is RegexToken.Atom || next is RegexToken.Group
    }

    private fun flattenTreeForConcats(regex: Regex): Regex {
        return when (regex) {
            is Regex.Atom -> regex
            is Regex.Star -> Regex.Star(flattenTreeForConcats(regex.expression))
            is Regex.Union -> {
                val newExpressions = regex.expressions.map { flattenTreeForConcats(it) }
                Regex.Union(newExpressions.toSet())
            }

            is Regex.Concat -> {
                val flattenExpressions = mutableListOf<Regex>()
                val newExpressions = regex.expressions.map { flattenTreeForConcats(it) }
                for (expression in newExpressions) {
                    when (expression) {
                        is Regex.Concat -> flattenExpressions.addAll(expression.expressions)
                        else -> flattenExpressions.add(expression)
                    }
                }

                Regex.Concat(flattenExpressions)
            }
        }
    }

    private fun flattenTreeForUnions(regex: Regex): Regex {
        return when (regex) {
            is Regex.Atom -> regex
            is Regex.Star -> Regex.Star(flattenTreeForUnions(regex.expression))
            is Regex.Concat -> {
                val newExpressions = regex.expressions.map { flattenTreeForUnions(it) }
                Regex.Concat(newExpressions)
            }

            is Regex.Union -> {
                val flattenExpressions = mutableListOf<Regex>()
                val newExpressions = regex.expressions.map { flattenTreeForUnions(it) }
                for (expression in newExpressions) {
                    when (expression) {
                        is Regex.Union -> flattenExpressions.addAll(expression.expressions)
                        else -> flattenExpressions.add(expression)
                    }
                }

                Regex.Union(flattenExpressions.toSet())
            }
        }
    }
}

private data class LastOperators(
    var lastUnion: Int? = null,
    var lastConcatAfter: Int? = null,
    var lastStar: Int? = null
)

private const val SOMETHING_WRONG_ERROR = "Something is wrong at %d"