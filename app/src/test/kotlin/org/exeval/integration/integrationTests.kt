package org.exeval.integration
import org.exeval.input.SimpleLocation
import org.exeval.parser.AnalyzedGrammar
import kotlin.test.Test

import org.exeval.parser.Grammar
import org.exeval.parser.Production
import org.exeval.parser.Parser
import org.exeval.parser.interfaces.ParseTree


class IntegrationTests() {

//    private fun makeLeaf(value: String): Leaf<String> {
//        return Leaf<String>(
//            symbol = value,
//            startLocation = SimpleLocation(0, 0),
//            endLocation = SimpleLocation(0, 0)
//        )
//    }
//
//    private fun makeBranch(production: Production<String>, children: List<Tree<String>>): Branch<String> {
//        return Branch<String>(
//            production = production,
//            children = children,
//            startLocation = SimpleLocation(0, 0),
//            endLocation = SimpleLocation(0, 0)
//        )
//    }
//
//    private fun makeTree(leaf: Leaf<String>, production: Production<String>, children: List<Tree<String>>): Tree<String> {
//        return Tree<String>(leaf, makeBranch(production, children))
//    }

    val helloWorld = Grammar<kotlin.String>(startSymbol = "hello", endOfParse = "!",
        productions = listOf(
            Production<kotlin.String>("greeting", listOf("simpleGreeting who")),
            Production<kotlin.String>("simpleGreeting", listOf("hello", "hi", "good morning")),
            Production<kotlin.String>("who", listOf("everybody", "gathered", "world")),
        )
    )

    val brackets = Grammar<String>(startSymbol = "", endOfParse = "",
        productions = listOf(
            Production<String>("S", listOf("{S}")),
            Production<String>("S", listOf("SS")),
            Production<String>("S", listOf("")),
            )
    )

    val logical = Grammar<String>(startSymbol = "(", endOfParse = ")",
        productions = listOf(
            Production<String>("expr", listOf("(expr)", "expr or expr", "expr and expr", "not expr", "token")),
            Production<String>("token", listOf("number", "boolean"))
        ))

    val declarations = Grammar<String>(startSymbol = "let", endOfParse = ";",
        productions = listOf(
            Production<String>("declaration", listOf("let assignment", "let mut assignment", "let mut identifier")),
            Production<String>("assignment", listOf("identifier = value")),
            Production<String>("identifier", listOf("name: type")),
            Production<String>("name", listOf("id")),
            Production<String>("type", listOf("Int", "Nope", "Bool")),
            Production<String>("value", listOf("number", "boolean", "empty")),

            )
    )

    val conditional = Grammar<String>(startSymbol = "if", endOfParse = ";",
        productions = listOf(
            Production<String>("expr", listOf("if-expr", "expr")),
            Production<String>("if-expr", listOf("if expr then expr", "if expr then expr else expr")),
            Production<String>("expr", listOf("(expr)", "expr; expr", "()"))
        )
    )

    val identifiers = Grammar<String>(startSymbol = "", endOfParse = "",
        productions = listOf(
            Production<String>("identifier", listOf("lowercase body")),
            Production<String>("body", listOf("symbol body", "symbol")),
            Production<String>("symbol", listOf("lowercase", "uppercase", "number")),
            Production<String>("lowercase", listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")),
            Production<String>("uppercase", listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")),
            Production<String>("number", listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")),
        )
    )

    val looping = Grammar<String>(startSymbol = "loop", endOfParse =  ";",
        productions = listOf(
            Production<String>("expr", listOf("loop {expr}")),
            Production<String>("expr", listOf("expr; expr", "{expr}", "(expr)", "break-statement", "()")),
            Production<String>("break-statement", listOf("break", "break (expr)"))
        )
    )

    val functions = Grammar<String>(startSymbol = "foo", endOfParse = "",
        productions = listOf(
            Production<String>("function", listOf("foo name (params) returnType equals body")),
            Production<String>("name", listOf("identifier")),
            Production<String>("params", listOf("identifier: type, params")),
            Production<String>("returnType", listOf("arrow type")),
            Production<String>("arrow", listOf("->")),
            Production<String>("type", listOf("Int", "Bool", "Nope")),
            Production<String>("equals", listOf("=")),
            Production<String>("body", listOf("expr;", "{expressions}")),
            Production<String>("expressions", listOf("expr; expr", "{expr}"))
        )
    )

    val simpleRegex = Grammar<String>(startSymbol = "", endOfParse = "",
        productions = listOf(
            Production<String>("text", listOf("symbol text", "symbol", "quantified-symbol", "quantified-symbol text")),
            Production<String>("quantified-symbol", listOf("(symbol quantifier)")),
            Production<String>("symbol", listOf("meta", "literal")),
            Production<String>("literal", listOf("\\d", "\\w")),
            Production<String>("meta", listOf(".", "\\")),
            Production<String>("quantifier", listOf("+", "*", "?"))
        )
    )

    @Test
    fun helloWorldTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun bracketTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun LogicTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun declarationTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun conditionalTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun identiferTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun loopTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun functionTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }

    @Test
    fun regexTest<String>(leafs: List<Leaf<String>>, expected: ParseTree<String>, grammar: Grammar<String>) {
        val parser = Parser<String>(AnalyzedGrammar<String>(nullable = _, firstProduct = _, grammar))

        assert(parser.run(leafs) == expected)
    }
