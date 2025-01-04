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
            val functionsList = unwrapList<FunctionDeclaration>(children[0], SimpleFunctionDefinitionSymbol, input) +
            unwrapList<FunctionDeclaration>(children[0], BlockFunctionDefinitionSymbol, input)

            astNode = Program(functionsList)
        } else if (symbol === SimpleFunctionDefinitionSymbol || symbol === BlockFunctionDefinitionSymbol) {
            var name: String? = null
            var parameters: List<Parameter> = listOf()
            var returnType: Type? = null
            var body: Expr? = null

            var functionDeclaration: ParseTree<GrammarSymbol> = children[0]

            val declarationChildren = when (functionDeclaration) {
                is Leaf -> listOf()
                is Branch -> functionDeclaration.children
            }

            for (child in declarationChildren) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype || childSymbol === TokenCategories.IdentifierEntrypoint) {
                    name = getNodeText(child, input)
                } else if (childSymbol === FunctionParamsSymbol) {
                    parameters = unwrapList<Parameter>(child, FunctionParamSymbol, input)
                } else if (childSymbol === TypeSymbol) {
                    returnType = getType(child, input)
                }
            }

            body = createAux(children[1], input) as Expr

            astNode = FunctionDeclaration(name!!, parameters, returnType!!, body)
        } else if (symbol === FunctionParamSymbol) {
            var name: String? = null
            var type: Type? = null

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === TypeSymbol) {
                    type = getType(child, input)
                }
            }
            astNode = Parameter(name!!, type!!)
        } else if (symbol === ExpressionSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === SimpleExpressionSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === ValueSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === TokenCategories.LiteralInteger) {
            astNode = IntLiteral(getNodeText(node, input).toLong())
        } else if (symbol === TokenCategories.LiteralBoolean) {
            astNode = BoolLiteral(getNodeText(node, input) == "true")
        } else if (symbol === TokenCategories.LiteralNope) {
            astNode = NopeLiteral()
        } else if (symbol === VariableDeclarationSymbol) {
            var name: String? = null
            var type: Type? = null
            var expression: Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === TypeSymbol) {
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
                } else if (childSymbol === TypeSymbol) {
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
        } else if (symbol === IfSymbol) {
            val conditionNode = createAux(children[1], input) as Expr
            val thenNode = createAux(children[3], input) as Expr
            val elseNode = children.getOrNull(5)?.let { createAux(it, input) as Expr }
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
        } else if (symbol === BreakSymbol) {
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
        } else if (symbol === AllocationSymmbol) {
            val typeIndex = 1
            val argumentsIndex = 3

            if (children.size != 4) {
                throw IllegalStateException("AllocationSymmbol $locationRange")
            }

            val type = getType(children[typeIndex], input) ?: throw IllegalStateException("AllocationSymmbol $locationRange")
            val arguments = unwrapList<Argument>(children[argumentsIndex], ExpressionSymbol, input)

            astNode = MemoryNew(type, arguments)
        } else if (symbol === DeallocationSymbol) {
            val exprIndex = 1

            if (children.size != 2) {
                throw IllegalStateException("DeallocationSymbol $locationRange")
            }

            astNode = MemoryDel(createAux(children[exprIndex], input) as Expr)
        } else if (symbol === ArrayAcessSymbol) {
            astNode = processArrayAcess(children, input) ?: throw IllegalStateException("ArrayAcessSymbol $locationRange")
        }
        else {
            throw IllegalStateException("$symbol $locationRange")
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

    private fun processArrayAcess(children: List<ParseTree<GrammarSymbol>>, input: Input): Expr? {
        fun processNestedArray(arrayExpr: Expr, indexSymbol: ParseTree<GrammarSymbol>, input: Input) : Expr? {
            val indexExprIndex = 1
            val nextIndexExpr = 3
            val sizeForNestedArrayAccess = 4
            val sizeForSingleArrayAccess = 3

            val subChildren = when (indexSymbol) {
                is Leaf -> listOf()
                is Branch -> indexSymbol.children
            }

            if (sizeForSingleArrayAccess != subChildren.size && sizeForNestedArrayAccess != subChildren.size) {
                return null
            }

            val subIndexExpr = createAux(subChildren[indexExprIndex], input) as Expr
            val acessExpr = ArrayAccess(arrayExpr, subIndexExpr)

            if (sizeForSingleArrayAccess == subChildren.size) {
                return acessExpr
            } else if (sizeForNestedArrayAccess == subChildren.size) {
                return processNestedArray(acessExpr, subChildren[nextIndexExpr], input)
            }

            return null
        }

        var arrayExpr: Expr? = null

        for (child in children) {
            val childSymbol = getSymbol(child)

            if (childSymbol === ExpressionSymbol) {
                arrayExpr = createAux(child, input) as Expr
            } else if (childSymbol === FunctionCallSymbol) {
                arrayExpr = createAux(child, input) as Expr
            } else if (childSymbol === TokenCategories.IdentifierNontype) {
                arrayExpr = VariableReference(getNodeText(child, input))
            } else if (childSymbol === ArrayIndexSymbol) {
                return arrayExpr?.let { processNestedArray(it, child, input) }
            }
        }

        return null
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

    private fun getArrayType(node: ParseTree<GrammarSymbol>, input: Input): Type? {
        val children = when (node) {
            is Leaf -> listOf()
            is Branch -> node.children
        }

        val countOfArrayProductions = 3
        val typeChildIndex = 1

        if (children.size != countOfArrayProductions) {
            return null
        }

        return getType(children[typeChildIndex], input)
            ?. let { ArrayType(it) }
    }

    private fun getType(node: ParseTree<GrammarSymbol>, input: Input): Type? {
        if (getSymbol(node) != TypeSymbol) {
            return null
        }

        return when (getNodeText(node, input)) {
            "Int" -> IntType
            "Bool" -> BoolType
            "Nope" -> NopeType
            else -> getArrayType(node, input)
        }
    }
}