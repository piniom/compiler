package org.exeval.ast

import org.exeval.ast.interfaces.AstCreator
import org.exeval.input.interfaces.Input
import org.exeval.parser.grammar.*
import org.exeval.parser.interfaces.ParseTree
import org.exeval.utilities.LocationRange
import org.exeval.utilities.TokenCategories
import kotlin.reflect.KClass
import kotlin.reflect.cast
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
            val functionsList = unwrapFunctions(children[0], input, FunctionDeclaration::class)
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
                    parameters = unwrapList<Parameter>(child, FunctionParamSymbol, input, Parameter::class)
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
            var variableExpr: AssignableExpr? = null
            var valueExpr: Expr? = null

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    variableExpr = VariableReference(getNodeText(child, input))
                } else if (childSymbol === ExpressionSymbol) {
                    valueExpr = createAux(child, input) as Expr
                } else if (childSymbol === ArrayAccessSymbol) {
                    variableExpr = createAux(child, input) as AssignableExpr
                }
            }
            astNode = Assignment(variableExpr!!, valueExpr!!)
        } else if (symbol === FunctionCallSymbol) {
            var name: String? = null
            var arguments: List<Argument> = listOf()
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child, input)
                } else if (childSymbol === FunctionCallArgumentsSymbol) {
                    arguments = parseFunctionCallArguments(child, input)
                }
            }

            astNode = FunctionCall(name!!, arguments)
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
        } else if (symbol === AllocationSymbol) {
            val typeIndex = 1
            val argumentsIndex = 3
            val productionsCount = 5

            if (children.size != productionsCount) {
                throw IllegalStateException("AllocationSymmbol $locationRange")
            }

            val type = getType(children[typeIndex], input) ?: throw IllegalStateException("AllocationSymmbol $locationRange")
            val arguments = parseFunctionCallArguments(children[argumentsIndex], input)

            astNode = MemoryNew(type, arguments)
        } else if (symbol === DeallocationSymbol) {
            val exprIndex = 1

            if (children.size != 2) {
                throw IllegalStateException("DeallocationSymbol $locationRange")
            }

            astNode = MemoryDel(createAux(children[exprIndex], input) as Expr)
        } else if (symbol === ArrayAccessSymbol) {
            astNode = processArrayAccess(children, input) ?: throw IllegalStateException("ArrayAcessSymbol $locationRange")
        }
        else {
            throw IllegalStateException("$symbol $locationRange")
        }
        locationsMap.put(astNode, locationRange)
        return astNode
    }

    private fun <T : ASTNode> unwrapList(
        head: ParseTree<GrammarSymbol>,
        correspondingSymbol: GrammarSymbol,
        input: Input,
        wantedNodeClass: KClass<T>
    ): List<T> {
        val subTrees = findSubTrees(head, correspondingSymbol)
        val res = subTrees.map { tree ->
            val node = createAux(tree, input)
            wantedNodeClass.cast(node)
        }
        return res
    }

    private fun processArrayAccess(children: List<ParseTree<GrammarSymbol>>, input: Input): Expr? {
        fun processNestedArray(arrayExpr: Expr, indexSymbol: ParseTree<GrammarSymbol>, input: Input) : Expr? {
            val nextIndexExpr = 3
            val indexExprIndex = 1
            val sizeForNestedArrayAccess = 4
            val sizeForSingleArrayAccess = 3

            val subChildren = when (indexSymbol) {
                is Leaf -> listOf()
                is Branch -> indexSymbol.children
            }

            val isNestedArrayAccess = subChildren.size == sizeForNestedArrayAccess
            val isSingleArrayAccess = subChildren.size == sizeForSingleArrayAccess

            if (!isSingleArrayAccess && !isNestedArrayAccess) {
                return null
            }

            val subIndexExpr = createAux(subChildren[indexExprIndex], input) as Expr
            val accessExpr = ArrayAccess(arrayExpr, subIndexExpr)

            if (sizeForSingleArrayAccess == subChildren.size) {
                return accessExpr
            } else if (sizeForNestedArrayAccess == subChildren.size) {
                return processNestedArray(accessExpr, subChildren[nextIndexExpr], input)
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

    private fun <T : ASTNode> unwrapFunctions(
        head: ParseTree<GrammarSymbol>,
        input: Input,
        wantedNodeClass: KClass<T>
    ): List<T> {
        val res = mutableListOf<T>()

        if (head is Branch && head.production.left == FunctionsDeclarationsSymbol) {
            for (child in head.children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === SimpleFunctionDefinitionSymbol ||
                    childSymbol === BlockFunctionDefinitionSymbol ||
                    childSymbol === ForeignFunctionDeclarationSymbol) {
                    val node = createAux(child, input)
                    res.add(wantedNodeClass.cast(node))
                } else if (childSymbol === FunctionsDeclarationsSymbol) {
                    res.addAll(unwrapFunctions(child, input, wantedNodeClass))
                } else {
                    continue
                }
            }
        }

        return res
    }


    private fun isVariableAssignment(node: ParseTree<GrammarSymbol>): Boolean {
        if (node !is Branch) return false

        val children = node.children
        if (children.isEmpty()) return false

        val firstChildSymbol = getSymbol(children[0])
        return when (firstChildSymbol) {
            SimpleExpressionSymbol -> {
                val simpleExpressionChildren = (children[0] as? Branch)?.children ?: return false
                val secondChildSymbol = getSymbol(simpleExpressionChildren[0])
                secondChildSymbol === VariableAssignmentSymbol
            }
            else -> false
        }
    }

    private fun parseFunctionCallArguments(node: ParseTree<GrammarSymbol>, input: Input): List<Argument> {
        val arguments = mutableListOf<Argument>()

        val children = when (node) {
            is Leaf -> listOf()
            is Branch -> node.children
        }

        for (child in children) {
            val childSymbol = getSymbol(child)

            when {
                childSymbol === ExpressionSymbol && isVariableAssignment(child) -> {
                    val variableAssignment = createAux(child, input) as Assignment
                    arguments.add(NamedArgument(getNameOfVariableToAssign(variableAssignment.variable), variableAssignment.value))
                }
                childSymbol === ExpressionSymbol -> {
                    arguments.add(PositionalArgument(createAux(child, input) as Expr))
                }
                childSymbol === FunctionCallArgumentsSymbol -> {
                    arguments.addAll(parseFunctionCallArguments(child, input))
                }
            }
        }

        return arguments
    }

    private fun getNameOfVariableToAssign(variable: Expr): String {
        return when (variable) {
            is ArrayAccess -> getNameOfVariableToAssign(variable.array)
            is VariableReference -> variable.name
            else -> throw IllegalStateException("Trying to assign to unsupported node type: ${variable::class}")
        }
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