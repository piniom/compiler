package org.exeval.ast

import org.exeval.ast.interfaces.AstCreator
import org.exeval.input.interfaces.Input
import org.exeval.parser.grammar.*
import org.exeval.parser.interfaces.ParseTree
import org.exeval.utilities.LocationRange
import org.exeval.utilities.TokenCategories
import org.exeval.utilities.TokenCategories as Token

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
                } else if (childSymbol === TokenCategories.IdentifierType) {
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
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    type = getType(child, input)
                }
            }
            astNode = Parameter(name!!, type!!)
        } else if (symbol === ExpressionSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === SimpleExpressionSymbol) {
            astNode = createAux(children[0], input)
        } else if (symbol === VariableReferenceSymbol) {
            astNode = VariableReference(getNodeText(children[0], input))
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
        } else if (symbol === IfSymbol) {
            val conditionNode = createAux(children[1], input) as Expr
            val thenNode = createAux(children[3], input) as Expr
            val elseNode = children.getOrNull(5)?.let { createAux(it, input) as Expr }
            astNode = Conditional(conditionNode, thenNode, elseNode)
        } else if (symbol === LoopSymbol) {
            var identifier: String? = null
            var body: Expr? = null

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    identifier = getNodeText(child, input)
                } else if (childSymbol === ExpressionSymbol || childSymbol === ExpressionBlockSymbol) {
                    body = createAux(child, input) as Expr
                }
            }

            if (body == null) {
                throw IllegalStateException("Loop body is missing in $locationRange")
            }

            astNode = Loop(identifier, body)
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
            val exprs = mutableListOf<Expr>()

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === ExpressionBlockSymbol.ExpressionChainSymbol) {
                    exprs.addAll(parseExpressionChain(child, input))
                } else if (childSymbol === SimpleExpressionSymbol || childSymbol === ExpressionSymbol) {
                    exprs.add(createAux(child, input) as Expr)
                }
            }

            astNode = Block(exprs)
        } else if (symbol === ArithmeticExpressionSymbol) {
            val expr: Expr
            when {
                children.size == 5 && getSymbol(children[0]) == Token.PunctuationLeftRoundBracket -> {
                    val innerExpression = createAux(children[1], input) as Expr
                    val operator = parseBinaryOperator(children[3])
                    val right = createAux(children[4], input) as Expr
                    expr = BinaryOperation(innerExpression, operator, right)
                }
                children.size == 3 && getSymbol(children[0]) == Token.PunctuationLeftRoundBracket -> {
                    expr = createAux(children[1], input) as Expr
                }
                children.size == 3 -> {
                    val left = createAux(children[0], input) as Expr
                    val operator = parseBinaryOperator(children[1])
                    val right = createAux(children[2], input) as Expr
                    expr = BinaryOperation(left, operator, right)
                }
                children.size == 2 -> {
                    val operator = parseUnaryOperator(children[0])
                    val operand = createAux(children[1], input) as Expr
                    expr = UnaryOperation(operator, operand)
                }
                else -> throw IllegalStateException("Invalid structure for ArithmeticExpressionSymbol: ${children.size}")
            }

            astNode = expr
        } else {
            throw IllegalStateException("${symbol} ${locationRange}")
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

    private fun parseBinaryOperator(node: ParseTree<GrammarSymbol>): BinaryOperator {
        val operatorNode = when (node) {
            is Branch -> node.children[0]
            is Leaf -> node
        }

        return when (getSymbol(operatorNode)) {
            Token.OperatorPlus -> BinaryOperator.PLUS
            Token.OperatorMinus -> BinaryOperator.MINUS
            Token.OperatorStar -> BinaryOperator.MULTIPLY
            Token.OperatorDivision -> BinaryOperator.DIVIDE
            Token.OperatorOr -> BinaryOperator.OR
            Token.OperatorAnd -> BinaryOperator.AND
            Token.OperatorGreater -> BinaryOperator.GT
            Token.OperatorLesser -> BinaryOperator.LT
            Token.OperatorGreaterEqual -> BinaryOperator.GTE
            Token.OperatorLesserEqual -> BinaryOperator.LTE
            Token.OperatorEqual -> BinaryOperator.EQ
            Token.OperatorNotEqual -> BinaryOperator.NEQ
            else -> throw IllegalStateException("Unknown binary operator: ${getSymbol(operatorNode)}")
        }
    }


    private fun parseUnaryOperator(node: ParseTree<GrammarSymbol>): UnaryOperator {
        val operatorNode = when (node) {
            is Branch -> node.children[0]
            is Leaf -> node
        }

        return when (getSymbol(operatorNode)) {
            Token.OperatorMinus -> UnaryOperator.MINUS
            Token.OperatorNot -> UnaryOperator.NOT
            else -> throw IllegalStateException("Unknown unary operator: ${getSymbol(operatorNode)}")
        }
    }



    private fun parseExpressionChain(node: ParseTree<GrammarSymbol>, input: Input): List<Expr> {
        val expressions = mutableListOf<Expr>()

        val children = when (node) {
            is Leaf -> listOf()
            is Branch -> node.children
        }

        for (child in children) {
            val childSymbol = getSymbol(child)

            when (childSymbol) {
                SimpleExpressionSymbol -> expressions.add(createAux(child, input) as Expr)
                ExpressionBlockSymbol -> expressions.add(createAux(child, input) as Expr)
                ExpressionBlockSymbol.ExpressionChainSymbol -> expressions.addAll(parseExpressionChain(child, input))
                BlockFunctionDefinitionSymbol -> expressions.add(createAux(child, input) as Expr)
                LoopSymbol -> expressions.add(createAux(child, input) as Expr)
                IfSymbol -> expressions.add(createAux(child, input) as Expr)
            }
        }

        return expressions
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