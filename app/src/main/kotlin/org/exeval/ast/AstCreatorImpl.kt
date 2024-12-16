package org.exeval.ast

import org.exeval.ast.interfaces.AstCreator
import org.exeval.input.interfaces.Input
import org.exeval.parser.grammar.*
import org.exeval.parser.interfaces.ParseTree
import org.exeval.utilities.LocationRange
import org.exeval.utilities.TokenCategories

typealias Branch = ParseTree.Branch<GrammarSymbol>
typealias Leaf = ParseTree.Leaf<GrammarSymbol>

class AstCreatorImpl : AstCreator<GrammarSymbol> {

    private var locationsMap: MutableMap<ASTNode, LocationRange> = mutableMapOf()

    override fun create(parseTree: ParseTree<GrammarSymbol>, input: Input): AstInfo {
        return AstInfo(createAux(parseTree, input), locationsMap)
    }

    private fun createAux(node: ParseTree<GrammarSymbol>, input: Input): ASTNode {
        val locationRange = LocationRange(node.startLocation, node.endLocation)
        val symbol = getSymbol(node)

        val children = when (node) {
            is Leaf -> listOf()
            is Branch -> node.children
        }

        val astNode: ASTNode
        if (symbol === ProgramSymbol) {
            val functionsList = unwrapList<FunctionDeclaration>(children[0], FunctionDeclarationSymbol, input)
            astNode = Program(functionsList)
        } else if (symbol === FunctionDeclarationSymbol) {
            var name: String? = null
            var parameters: List<Parameter> = listOf()
            var returnType: Type? = null
            var body: Expr? = null

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype || childSymbol === TokenCategories.IdentifierEntrypoint) {
                    name = getNodeText(child, input)
                } else if (childSymbol === FunctionParamsSymbol) {
                    parameters = unwrapList<Parameter>(child, FunctionParamSymbol, input)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    returnType = getType(child, input)
                } else if (childSymbol === ExpressionSymbol) {
                    body = createAux(child, input) as Expr
                }
            }

            astNode = FunctionDeclaration(name!!, parameters, returnType!!, body!!)
        }
        else if(symbol === ForeignFunctionDeclarationSymbol){
            var name: String? = null
            var parameters: List<Parameter> = listOf()
            var returnType: Type? = null
            for (child in children){
                val childSymbol = getSymbol(child)

                if(childSymbol === TokenCategories.IdentifierNontype){
                    name = getNodeText(child, input)
                } else if(childSymbol === FunctionParamsSymbol){
                    parameters = unwrapList<Parameter>(child, FunctionParamSymbol, input)
                } else if (childSymbol === TokenCategories.IdentifierType){
                    returnType = getType(child, input)
                }
            }
            TODO("Uncomment after merge")
            // astNode = ForeignFunctionDeclaration(name!!, parameters, returnType!!)
        }
        else if (symbol === FunctionParamSymbol) {
            var name: String? = null
            var type: Type? = null

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    type = getType(child, input)
                }
            }
            astNode = Parameter(name!!, type!!)
        } else if (symbol === ExpressionSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === SimpleExpressionSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === LastExpressionInBlockSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === ValueSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === TokenCategories.LiteralInteger) {
            astNode = IntLiteral(getNodeText(node, input).toLong())
        } else if (symbol === TokenCategories.LiteralBoolean) {
            astNode = BoolLiteral(getNodeText(node, input) == "true")
        } else if (symbol === TokenCategories.LiteralNope) {
            astNode = NopeLiteral
        } else if (symbol === VariableDeclarationSymbol) {
            var name: String? = null
            var type: Type? = null
            var expression: Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    type = getType(child, input)
                } else if (childSymbol === ExpressionSymbol) {
                    expression = createAux(child, input) as Expr
                }
            }
            astNode = MutableVariableDeclaration(name!!, type!!, expression)
        } else if (symbol === ConstantDeclarationSymbol) {
            var name: String? = null
            var type: Type? = null
            var expression: Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    type = getType(child, input)
                } else if (childSymbol === ExpressionSymbol) {
                    expression = createAux(child, input) as Expr
                }
            }
            astNode = ConstantDeclaration(name!!, type!!, expression!!)
        } else if (symbol === VariableAssignmentSymbol) {
            var name: String? = null
            var expression: Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === ExpressionSymbol) {
                    expression = createAux(child, input) as Expr
                }
            }
            astNode = Assignment(name!!, expression!!)
        } else if (symbol === FunctionCallSymbol) {
            var name: String? = null
            var arguments: List<Argument> = listOf()
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === FunctionCallArgumentsSymbol) {
                    arguments = unwrapList<Argument>(child, ExpressionSymbol, input)
                }
            }
            astNode = FunctionCall(name!!, arguments)
        } else if (symbol === FunctionCallArgumentsSymbol) {
            var name: String? = null
            var arguments: List<Argument> = listOf()
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === FunctionCallArgumentsSymbol) {
                    arguments = unwrapList<Argument>(child, ExpressionSymbol, input)
                }
            }
            astNode = FunctionCall(name!!, arguments)
        } else if (symbol === FunctionCallArgumentsSymbol) {
            astNode = PositionalArgument(createAux(children[0], input) as Expr)
        } else if (symbol === IfThenSymbol || symbol == IfThenWithoutSemicolonSymbol) {
            val conditionNode = createAux(children[1], input) as Expr
            val thenNode = createAux(children[3], input) as Expr
            astNode = Conditional(conditionNode, thenNode, null)
        } else if (symbol === IfThenElseSymbol) {
            val conditionNode = createAux(children[1], input) as Expr
            val thenNode = createAux(children[3], input) as Expr
            val elseNode = createAux(children[5], input) as Expr
            astNode = Conditional(conditionNode, thenNode, elseNode)
        } else if (symbol === LoopSymbol) {
            var identifier: String? = null
            var body: Block? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    identifier = getNodeText(child, input)
                } else if (childSymbol === ExpressionBlockSymbol) {
                    body = createAux(child, input) as Block
                }
            }
            astNode = Loop(identifier, body!!)
        } else if (symbol === BreakKeywordSymbol || symbol === BreakKeywordWithoutSemicolonSymbol || symbol === BreakExpressionSymbol) {
            var identifier: String? = null
            var expr: Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    identifier = getNodeText(child, input)
                } else if (childSymbol === ExpressionSymbol) {
                    expr = createAux(child, input) as Expr
                }
            }
            astNode = Break(identifier, expr)
        } else if (symbol === ExpressionBlockSymbol) {
            var exprs: List<Expr> = listOf()
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === FunctionCallArgumentsSymbol) {
                    exprs = unwrapList<Expr>(child, ExpressionSymbol, input)
                }
            }
            astNode = Block(exprs)
        } else {
            throw IllegalStateException()
        }
        locationsMap.put(astNode, locationRange)
        return astNode
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : ASTNode> unwrapList(
        head: ParseTree<GrammarSymbol>,
        correspondingSymbol: GrammarSymbol,
        input: Input,
    ): List<T> {
        val subTrees = findSubTrees(head, correspondingSymbol)
        val res = subTrees.map { tree ->
            val node = createAux(tree, input)
            node as T
        }
        return res
    }

    private fun findSubTrees(head: ParseTree<GrammarSymbol>, symbol: GrammarSymbol): List<ParseTree<GrammarSymbol>> {
        val res: MutableList<ParseTree<GrammarSymbol>> = mutableListOf()
        when (head) {
            is ParseTree.Leaf -> if (head.symbol == symbol) res.add(head)
            is ParseTree.Branch -> {
                if (head.production.left == symbol) {
                    res.add(head)
                } else {
                    for (child in head.children) {
                        res.addAll(findSubTrees(child, symbol))
                    }
                }
            }
        }

        return res
    }

    private fun getNodeText(node: ParseTree<GrammarSymbol>, input: Input): String {
        val prevLocation = input.location

        val start = node.startLocation
        val end = node.endLocation

        input.location = start

        val builder = StringBuilder()

        while (input.location != end) {
            builder.append(input.nextChar())
        }

        input.location = prevLocation
        return builder.toString()
    }

    private fun getSymbol(node: ParseTree<GrammarSymbol>): GrammarSymbol {
        return when (node) {
            is Leaf -> node.symbol
            is Branch -> node.production.left
        }
    }

    private fun getType(node: ParseTree<GrammarSymbol>, input: Input): Type? {
        val name = getNodeText(node, input)

        if (name == "Int") {
            return IntType
        } else if (name == "Bool") {
            return BoolType
        } else if (name == "Nope") {
            return NopeType
        }
        return null
    }
}