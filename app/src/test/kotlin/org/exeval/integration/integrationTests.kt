package org.exeval.integration
import org.exeval.input.SimpleLocation
import org.exeval.parser.AnalyzedGrammar
import kotlin.test.Test

import org.exeval.parser.Grammar
import org.exeval.parser.Production
import org.exeval.parser.Parser
import org.exeval.parser.interfaces.ParseTree
import org.exeval.parser.interfaces.ParseTree.Leaf
import org.exeval.parser.interfaces.ParseTree.Branch
import org.exeval.parser.utilities.GrammarAnalyser


class IntegrationTests() {

    private fun <S>makeLeaf(value: S, pos: Int): Leaf<S> {
        return Leaf<S>(
            symbol = value,
            startLocation = SimpleLocation(0, pos),
            endLocation = SimpleLocation(0, pos)
        )
    }

    private fun <S>makeBranch(production: Production<S>, children: List<ParseTree<S>>, start: Int, end: Int): Branch<S> {
        return Branch<S>(
            production = production,
            children = children,
            startLocation = SimpleLocation(0, start),
            endLocation = SimpleLocation(0, end)
        )
    }

    // "hello world!"
    @Test
    fun helloWorldTest() {
        //grammar
        val productions = listOf(
            Production<String>("greeting", listOf("simpleGreeting who!")),
            Production<String>("simpleGreeting", listOf("hello", "hi", "good morning")),
            Production<String>("who", listOf("everybody", "gathered", "world")),
        )
        val grammar = Grammar<String>(startSymbol = "hello", endOfParse = "!", productions)
        //prepare
        val leafs = listOf(makeLeaf("hello", 0), makeLeaf("world", 1), makeLeaf("!", 2))
        val expected = makeBranch(productions[0], listOf(
            makeBranch(productions[1], listOf(makeLeaf("hello", 0)), 0, 0),
            makeBranch(productions[2], listOf(makeLeaf("world", 1)), 1, 1),
            makeLeaf("!", 2)),
            0, 1)
        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "{{{e}}{e}}"
    @Test
    fun bracketTest() {
        //grammar
        val productions = listOf(
            Production<String>("S", listOf("{S}")),
            Production<String>("S", listOf("{S}{S}")),
            Production<String>("S", listOf("S{e}")),
            Production<String>("S", listOf("{e}")),
        )
        val grammar = Grammar<String>(startSymbol = "{", endOfParse = "}", productions)
        //prepare
        val leafs = listOf(makeLeaf("{", 0), makeLeaf("{", 1), makeLeaf("{", 2), makeLeaf("e", 3), makeLeaf("}", 4),
            makeLeaf("}", 5), makeLeaf("{", 6), makeLeaf("e", 7), makeLeaf("}", 8), makeLeaf("}", 9))
        val expected = makeBranch(productions[0], listOf(
            makeLeaf("{", 0),
            makeBranch(productions[1], listOf(
                makeBranch(productions[0], listOf(
                    makeLeaf("{", 1),
                    makeBranch(productions[3], listOf(makeLeaf("{", 2), makeLeaf("e", 3), makeLeaf("}", 4)), 2, 4),
                    makeLeaf("}", 5)
                ), 1, 5),
                makeBranch(productions[3], listOf(makeLeaf("{", 6), makeLeaf("e", 7), makeLeaf("}", 8)), 6, 8)
            ), 1, 8),
            makeLeaf("}", 9)
        ), 0, 9)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "not (8 and 1) or false"
    @Test
    fun logicTest() {
        //grammar
        val productions = listOf(
            Production<String>("expr", listOf("(expr)", "expr or expr", "expr and expr", "token")),
            Production<String>("not-operator", listOf("not expr")),
            Production<String>("token", listOf("number", "boolean"))
        )
        val grammar = Grammar<String>(startSymbol = "(", endOfParse = ")", productions)
        //prepare
        val leafs = listOf(makeLeaf("not", 0), makeLeaf("(", 1), makeLeaf("8", 2), makeLeaf("and", 3), makeLeaf("1", 4), makeLeaf(")", 5), makeLeaf("or", 6), makeLeaf("false", 7))
        val expected = makeBranch(productions[1], listOf(makeBranch(productions[0], listOf(
            makeLeaf("not", 0),
            makeBranch(productions[0], listOf(
                makeLeaf("(", 1),
                makeBranch(productions[0], listOf(
                    makeBranch(productions[2], listOf(makeLeaf("8", 2)), 2, 2),
                    makeLeaf("and", 3),
                    makeBranch(productions[2], listOf(makeLeaf("1", 4)), 4, 4)
                ), 2, 4),
                makeLeaf(")", 5)
            ), 1, 5),
            makeLeaf("or", 6),
            makeBranch(productions[2], listOf(makeLeaf("false", 7)), 7, 7)
        ), 1, 7)), 0, 7)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "let mut x: Int = 5"
    @Test
    fun declarationTest() {
        //grammar
        val productions = listOf(
            Production<String>("declaration", listOf("let assignment", "let mut assignment", "let mut identifier")),
            Production<String>("assignment", listOf("identifier equals value")),
            Production<String>("identifier", listOf("name type")),
            Production<String>("name", listOf("id:")),
            Production<String>("type", listOf("Int", "Nope", "Bool")),
            Production<String>("value", listOf("number", "boolean", "empty")),
            Production<String>("equals", listOf("=")),
            Production<String>("id", listOf("x", "y", "z", "a", "b")),
            Production<String>("number", listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0")),
            Production<String>("boolean", listOf("true", "false")),
            Production<String>("empty", listOf("Nope"))
        )
        val grammar = Grammar<String>(startSymbol = "let", endOfParse = ";", productions)
        //prepare
        val leafs = listOf(makeLeaf("let", 0), makeLeaf("mut", 1), makeLeaf("x", 2), makeLeaf(":", 3), makeLeaf("Int", 4), makeLeaf("=", 5), makeLeaf("5", 6))
        val expected = makeBranch(
            productions[0],
            listOf(
                makeLeaf("let", 0),
                makeLeaf("mut", 1),
                makeBranch(
                    productions[1], listOf(
                        makeBranch(
                            productions[2], listOf(
                                makeBranch(
                                    productions[3], listOf(
                                        makeBranch(productions[7], listOf(makeLeaf("x", 2)), 2, 2),
                                        makeLeaf(":", 3)),
                                    2, 3),
                                        makeBranch(productions[4], listOf(makeLeaf("Int", 4)), 4, 4)),
                            2, 4),
                        makeBranch(productions[6], listOf(makeLeaf("=", 5)), 5, 5),
                        makeBranch(productions[5], listOf(
                                makeBranch(productions[8], listOf(makeLeaf("5", 6)), 6, 6))
                            , 6, 6)),
                    2, 6)),
            0, 6)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "if expr then () else expr;"
    @Test
    fun conditionalTest() {
        //grammar
        val productions = listOf(
            Production<String>("expr", listOf("if-expr", "expr")),
            Production<String>("if-expr", listOf("if expr then expr", "if expr then expr else expr")),
            Production<String>("expr", listOf("(expr)", "expr; expr", "()"))
        )
        val grammar = Grammar<String>(startSymbol = "if", endOfParse = ";", productions)
        //prepare
        val leafs = listOf(makeLeaf("if", 0), makeLeaf("expr", 1), makeLeaf("then", 2), makeLeaf("()", 3), makeLeaf("else", 4), makeLeaf("expr", 5))
        val expected = makeBranch(productions[1], listOf(
            makeLeaf("if", 0),
            makeLeaf("expr", 1),
            makeLeaf("then", 2),
            makeBranch(productions[2], listOf(makeLeaf("()", 3)), 3, 3),
            makeLeaf("else", 4),
            makeLeaf("expr", 5)), 0, 5)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "waNd34"
    @Test
    fun identiferTest() {
        //grammar
        val productions = listOf(
            Production<String>("identifier", listOf("lowercase body")),
            Production<String>("body", listOf("symbol body", "symbol")),
            Production<String>("symbol", listOf("lowercase", "uppercase", "number")),
            Production<String>("lowercase", listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")),
            Production<String>("uppercase", listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")),
            Production<String>("number", listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")),
        )
        val grammar = Grammar<String>(startSymbol = "", endOfParse = "", productions)
        //prepare
        val leafs = listOf(makeLeaf("w", 0), makeLeaf("a", 1), makeLeaf("N", 2), makeLeaf("d", 3), makeLeaf("3", 4), makeLeaf("4", 5))
        val expected = makeBranch( productions[0],
            listOf(
                makeBranch(productions[3], listOf(makeLeaf("w", 0)), 0, 0),
                makeBranch(productions[1], listOf(
                    makeBranch(productions[2], listOf(makeBranch(productions[3], listOf(makeLeaf("a", 1)), 1, 1)), 1, 1),
                    makeBranch(productions[1], listOf(
                        makeBranch(productions[2], listOf(makeBranch(productions[4], listOf(makeLeaf("N", 2)), 2, 2)), 2, 2),
                        makeBranch(productions[1], listOf(
                            makeBranch(productions[2], listOf(makeBranch(productions[3], listOf(makeLeaf("d", 3)), 3, 3)), 3, 3),
                            makeBranch(productions[1], listOf(
                                makeBranch(productions[2], listOf(makeBranch(productions[5], listOf(makeLeaf("3", 4)), 4, 4)), 4, 4),
                                makeBranch(productions[1], listOf(makeBranch(productions[2], listOf(makeBranch(productions[5], listOf(makeLeaf("4", 5)), 5, 5)), 5, 5)), 5, 5)
                            ), 4, 5),
                            ), 3, 5),
                    ), 2, 5),
                ), 1, 5),
            ),0, 5)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "loop {(); if expr then break}"
    @Test
    fun loopTest() {
        //grammar
        val productions = listOf(
            Production<String>("expr", listOf("loop {expr}")),
            Production<String>("expr", listOf("expr; expr", "{expr}", "if expr then expr", "break", "()")),
        )
        val grammar = Grammar<String>(startSymbol = "loop", endOfParse =  ";", productions)
        //prepare
        val leafs = listOf(makeLeaf("loop", 0), makeLeaf("{", 1), makeLeaf("()", 2), makeLeaf(";", 3), makeLeaf("if", 4),
            makeLeaf("expr", 5), makeLeaf("then", 6), makeLeaf("break", 7), makeLeaf("}", 8))
        val expected = makeBranch(productions[0], listOf(
            makeLeaf("loop", 0),
            makeLeaf("{", 1),
            makeBranch(productions[1],
            listOf(makeBranch(productions[1], listOf(
                makeBranch(productions[1], listOf(makeLeaf("()", 2)), 2, 2),
                makeLeaf(";", 3),
                makeBranch(productions[1], listOf(makeLeaf("if", 4), makeLeaf("expr", 5), makeLeaf("then", 6),
                    makeBranch(productions[1], listOf(makeLeaf("break", 7)), 7, 7)), 5, 7)
            ), 2, 7))
        ,1, 8),
            makeLeaf("}", 8)), 0, 8)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "foo identifier (identifier: Bool, identifier: Int) -> Nope expr;"
    @Test
    fun functionTest() {
        //grammar
        val productions = listOf(
            Production<String>("function", listOf("foo name (params) returnType body")),
            Production<String>("name", listOf("identifier")),
            Production<String>("params", listOf("identifier: type, params", "identifier: type")),
            Production<String>("returnType", listOf("arrow type")),
            Production<String>("arrow", listOf("->")),
            Production<String>("type", listOf("Int", "Bool", "Nope")),
            Production<String>("body", listOf("expr;")),
        )
        val grammar = Grammar<String>(startSymbol = "foo", endOfParse = "", productions)
        //prepare
        val leafs = listOf(makeLeaf("foo", 0), makeLeaf("identifier", 1), makeLeaf("(", 2), makeLeaf("identifier", 3), makeLeaf(":", 4),
            makeLeaf("Bool", 5), makeLeaf(",", 6), makeLeaf("identifier", 7), makeLeaf(":", 8), makeLeaf("Int", 9), makeLeaf(")", 10),
            makeLeaf("->", 11), makeLeaf("Nope", 12), makeLeaf("expr;", 13))
        val expected = makeBranch(productions[0], listOf(
            makeLeaf("foo", 0),
            makeBranch(productions[1], listOf(makeLeaf("identifier", 1)), 1, 1),
            makeLeaf("(", 2),
            makeBranch(productions[2], listOf(
                makeLeaf("identifier", 3),
                makeLeaf(":", 4),
                makeBranch(productions[5], listOf(makeLeaf("Bool", 5)), 5, 5),
                makeLeaf(",", 6)), 5, 5),
            makeBranch(productions[2], listOf(makeLeaf("identifier", 7), makeLeaf(":", 8), makeBranch(productions[5], listOf(makeLeaf("Int", 9)), 9, 9)), 7, 9),
            makeLeaf(")", 10),
            makeBranch(productions[3], listOf(
                makeBranch(productions[4], listOf(makeLeaf("->", 11)), 11, 11),
                makeBranch(productions[5], listOf(makeLeaf("Nope", 12)), 12, 12)), 11, 12),
            makeBranch(productions[7], listOf(makeLeaf("expr;", 13)), 13, 13)),
            0, 13)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }

    // "(\\d+)(.*)\\w\\w"
    @Test
    fun regexTest() {
        //grammar
        val productions = listOf(
            Production<String>("text", listOf("symbol text", "symbol", "quantified-symbol", "quantified-symbol text")),
            Production<String>("quantified-symbol", listOf("(symbol quantifier)")),
            Production<String>("symbol", listOf("meta", "literal")),
            Production<String>("literal", listOf("\\d", "\\w")),
            Production<String>("meta", listOf(".", "\\")),
            Production<String>("quantifier", listOf("+", "*", "?"))
        )
        val grammar = Grammar<String>(startSymbol = "", endOfParse = "", productions)
        //prepare
        val leafs = listOf(makeLeaf("(", 0), makeLeaf("\\d", 1), makeLeaf("+", 2), makeLeaf(")", 3), makeLeaf("(", 4),
            makeLeaf(".", 5), makeLeaf("*", 6), makeLeaf(")", 7), makeLeaf("\\w", 8), makeLeaf("\\w", 9))
        val expected = makeBranch(productions[0], listOf(
            makeBranch(productions[1], listOf(
                makeLeaf("(", 0),
                makeBranch(productions[2], listOf(makeBranch(productions[3], listOf(makeLeaf("\\d", 1)), 1, 1)), 1, 1),
                makeBranch(productions[5], listOf(makeLeaf("+", 2)), 2, 2),
                makeLeaf(")", 3)), 0, 3),
            makeBranch(productions[0], listOf(
                makeBranch(productions[1], listOf(
                    makeLeaf("(", 4),
                    makeBranch(productions[2], listOf(makeBranch(productions[3], listOf(makeLeaf(".", 5)), 5, 5)), 5, 5),
                    makeBranch(productions[5], listOf(makeLeaf("*", 6)), 6, 6),
                    makeLeaf(")", 7)), 4, 6),
                makeBranch(
                    productions[0],
                    listOf(
                        makeBranch(
                            productions[2], listOf(
                                makeBranch(productions[3], listOf(makeLeaf("\\w", 8)), 8, 8)
                            ), 8, 8
                        ),
                        makeBranch(productions[0], listOf(
                            makeBranch(productions[2], listOf(
                                makeBranch(productions[3], listOf(makeLeaf("\\w", 9)), 9, 9)
                            ), 9, 9)
                        ), 9, 9)
                    ), 8, 9,)
            ), 4, 9)
        ), 0, 9)

        val parser = Parser<String>(GrammarAnalyser.analyseGrammar((grammar)))
        //run
        assert(parser.run(leafs) == expected)
    }
}
