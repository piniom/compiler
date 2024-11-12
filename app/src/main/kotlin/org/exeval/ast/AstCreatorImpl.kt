package org.exeval.ast

import org.exeval.ast.interfaces.AstCreator
import org.exeval.parser.grammar.*
import org.exeval.utilities.LocationRange
import org.exeval.parser.interfaces.ParseTree
import org.exeval.utilities.TokenCategories

typealias Branch = ParseTree.Branch<GrammarSymbol>
typealias Leaf = ParseTree.Leaf<GrammarSymbol>

class AstCreatorImpl : AstCreator<GrammarSymbol> {

    private var locationsMap : MutableMap<ASTNode, LocationRange> = mutableMapOf()

    override fun create(parseTree: ParseTree<GrammarSymbol>): AstInfo {
        return AstInfo(createAux(parseTree), locationsMap)
    }

    private fun createAux(node: ParseTree<GrammarSymbol>) : ASTNode {
        val locationRange = LocationRange(node.startLocation, node.endLocation)
        val symbol = getSymbol(node)

        val children = when(node) {
            is Leaf -> listOf()
            is Branch -> node.children
        }

        val astNode
        if (symbol === ProgramSymbol) {
            val functionsList = unwrapList<FunctionDeclaration>(children[0])
            astNode = Program(functionsList)
        } else if (symbol === FunctionDeclarationSymbol) {
            var name : String? = null
            var parameters : List<Parameter> = listOf()
            var returnType : Type? = null
            var body : Expr? = null

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype || childSymbol === TokenCategories.IdentifierEntrypoint) {
                    name = getNodeText(child)
                } else if (childSymbol === FunctionParamsSymbol) {
                    parameters = unwrapList<Parameter>(child)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    returnType = getType(child)
                } else if (childSymbol === ExpressionSymbol) {
                    body = createAux(child) as Expr
                }
            }

            astNode = FunctionDeclaration(name!!, parameters, returnType!!, body!!)
        } else if (symbol === FunctionParamSymbol) {
            var name : String? = null
            var type : Type? = null

            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    type = getType(child)
                }
            }
            astNode = Parameter(name!!, type!!)
        } else if (symbol === ExpressionSymbol) {
            astNode = createAux(children[0])
        } else if (symbol === SimpleExpressionSymbol) {
            astNode = createAux(children[0])
        } else if (symbol === LastExpressionInBlockSymbol) {
            astNode = createAux(children[0])
        } else if (symbol === ValueSymbol) {
            astNode = createAux(children[0])
        } else if (symbol === TokenCategories.LiteralInteger) {
            astNode = IntLiteral(getNodeText(node).toInt())
        } else if (symbol === TokenCategories.LiteralBoolean) {
            astNode = BoolLiteral(getNodeText(node) == "true")
        } else if (symbol === TokenCategories.LiteralNope) {
            astNode = NopeLiteral
        } else if (symbol === VariableDeclarationSymbol) {
            var name : String? = null
            var type : Type? = null
            var expression : Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    type = getType(child)
                } else if (childSymbol === ExpressionSymbol) {
                    expression = createAux(child) as Expr
                }
            }
            astNode = MutableVariableDeclaration(name!!, type!!, expression)
        } else if (symbol === ConstantDeclarationSymbol) {
            var name : String? = null
            var type : Type? = null
            var expression : Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child)
                } else if (childSymbol === TokenCategories.IdentifierType) {
                    type = getType(child)
                } else if (childSymbol === ExpressionSymbol) {
                    expression = createAux(child) as Expr
                }
            }
            astNode = ConstantDeclaration(name!!, type!!, expression!!)
        } else if (symbol === VariableAssignmentSymbol) {
            var name : String? = null
            var expression : Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child)
                } else if (childSymbol === ExpressionSymbol) {
                    expression = createAux(child) as Expr
                }
            }
            astNode = Assignment(name!!, expression!!)
        } else if (symbol === FunctionCallSymbol) {
            var name : String? = null
            var arguments : List<Argument> = listOf()
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child)
                } else if (childSymbol === FunctionCallArgumentsSymbol) {
                    arguments = unwrapList<Argument>(child)
                }
            }
            astNode = FunctionCall(name!!, arguments)
        } else if (symbol === FunctionCallArgumentsSymbol) {
            var name : String? = null
            var arguments : List<Argument> = listOf()
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    name = getNodeText(child)
                } else if (childSymbol === FunctionCallArgumentsSymbol) {
                    arguments = unwrapList<Argument>(child)
                }
            }
            astNode = FunctionCall(name!!, arguments)
        } else if (symbol === FunctionCallArgumentsSymbol) {
            astNode = PositionalArgument(createAux(children[0]) as Expr)
        } else if (symbol === IfThenSymbol || symbol == IfThenWithoutSemicolonSymbol) {
            val conditionNode = createAux(children[1]) as Expr
            val thenNode = createAux(children[3]) as Expr
            astNode = Conditional(conditionNode, thenNode, null)
        } else if (symbol === IfThenElseSymbol) {
            val conditionNode = createAux(children[1]) as Expr
            val thenNode = createAux(children[3]) as Expr
            val elseNode = createAux(children[5]) as Expr
            astNode = Conditional(conditionNode, thenNode, elseNode)
        }  else if (symbol === LoopSymbol) {
            var identifier: String? = null
            var body : Expr? = null
            for (child in children) {
                val childSymbol = getSymbol(child)

                if (childSymbol === TokenCategories.IdentifierNontype) {
                    identifier = getNodeText(child)
                } else if (childSymbol === ExpressionSymbol) {
                    body = createAux(child) as Expr
                }
            }
            astNode = Loop(identifier, body)
        } else {
            TODO()
        }
        locationsMap.put(astNode, locationRange)
        return astNode
    }

    private fun <T> unwrapList(head : ParseTree<GrammarSymbol>): List<T> {
        TODO()
    }

    private fun getNodeText(head : ParseTree<GrammarSymbol>): String {
        TODO()
    }

    private fun getSymbol(node : ParseTree<GrammarSymbol>): GrammarSymbol {
        return when(node) {
            is Leaf -> node.symbol
            is Branch -> node.production.left
        }
    }

    private fun getType(node : ParseTree<GrammarSymbol>): Type? {
        val name = getNodeText(node)

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