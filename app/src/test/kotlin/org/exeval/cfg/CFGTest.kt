package org.exeval.cfg

import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.Tree
import org.exeval.ast.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CFGTest{
    class Node(
        override var branches: Pair<CFGNode,CFGNode?>?,
        override val trees: List<Tree>
    ) : CFGNode {}
    fun getCFG(@Suppress("UNUSED_PARAMETER")e:Expr):CFGNode{
        TODO("create cfg from ast")
    }
    fun branch(cfg:CFGNode):Boolean{
        /*
        if-else requires branching
        */
        //recursive function that detects branching
        fun recursive(n:CFGNode):Boolean{
            if(n.branches == null){
                return false
            }else if(n.branches!!.second == null){
                return recursive(n.branches!!.first)
            }else{
                return true
            }
        }
        return recursive(cfg)
    }
    fun loop(cfg:CFGNode):Boolean{
        /*prove that a loop occurs */
        val visited = mutableSetOf<CFGNode>()
        fun recursive(n:CFGNode):Boolean{
            if(n in visited){
                return true
            }
            visited += n
            if(n.branches == null){
                return false
            }else if(n.branches!!.second == null){
                return recursive(n.branches!!.first)
            }else{
                return recursive(n.branches!!.first) || recursive(n.branches!!.second!!)
            }
        }
        return recursive(cfg)
    }
    fun calculate(cfg:CFGNode,t:Tree):Boolean{
        //check for simple calculations
        //ASSUMPTION: cfg does not split here
        if(branch(cfg) || loop(cfg)){
            return false
        }

        val assignableContent = mutableMapOf<Assignable,Tree>()
        fun recursive(t:Tree):Tree{
            //transform tree by replacing Assignable with Tree in Assignable
            return when(t){
                is Assignable -> assignableContent.getValue(t)
                is UnaryOp -> UnaryOp(recursive(t),t.binaryOperationType)
                is BinaryOperation -> BinaryOperation(recursive(t.left), recursive(t.right), t.operation)
                else -> t
            }
        }
        fun iterative(){
            var n:CFGNode? = cfg
            while(n != null){
                n.trees.forEach{
                    if(it is Assigment){
                        assignableContent[it.destination] = recursive(it.value)
                    }
                }
                n = if(n.branches == null){null}else{n.branches!!.first}
            }
        }
        try {
            iterative()
        }
        catch(e:NoSuchElementException) {
            return false
        }
        return assignableContent.values.contains(t)
    }
    fun placement(cfg:CFGNode,t:Tree):Boolean{
        //check if expression is in branch
        //1 branching, no loops
        if(!branch(cfg) || loop(cfg)){
            return false
        }
        var i = cfg
        var b1:CFGNode
        var b2:CFGNode

        while(i.branches!!.second == null){
            i=i.branches!!.first
        }

        b1 = i.branches!!.first
        b2 = i.branches!!.second!!

        return calculate(b1,t) || calculate(b2,t)
    }
    @Test
    fun Validation(){
        /*prove that tests can pass*/
        //branch
        var cfg = Node(Pair(
            Node(null,listOf(Assigment(VirtualRegister(1),Constant(1)))),
            Node(null,listOf(Assigment(VirtualRegister(1),Constant(2))))
        ),listOf())
        assert(branch(cfg))

        //loop
        var assNode = Node(null,listOf(Assigment(VirtualRegister(1), Constant(0))))
        var loopNode = Node(null,listOf())
        var bodyNode = Node(Pair(loopNode,null),listOf())
        loopNode.branches = Pair(bodyNode,null)
        assNode.branches = Pair(loopNode,null)
        assert(loop(assNode))

        //calculate
        //1<-1+2
        //1
        val value = BinaryOperation(Constant(1), Constant(2), BinaryOperationType.ADD)
        val simple = Node(null,listOf(Assigment(VirtualRegister(1), BinaryOperation(
            Constant(1), Constant(2), BinaryOperationType.ADD))))
        assert(calculate(simple, value))
        //2 - spread over multiple trees
        val complex = Node(null,listOf(
            Assigment(VirtualRegister(2), Constant(2)),
            Assigment(VirtualRegister(1), BinaryOperation(
                Constant(1), VirtualRegister(2), BinaryOperationType.ADD))
        ))
        assert(calculate(complex, value))
        //3 - spread over multiple nodes
        val separated1 = Node(null,listOf(Assigment(VirtualRegister(2), Constant(2))))
        val separated2 = Node(null,listOf(Assigment(VirtualRegister(1), BinaryOperation(
            Constant(1), VirtualRegister(2), BinaryOperationType.ADD))))
        separated1.branches = Pair(separated2,null)
        assert(calculate(separated1, value))

        //placement
        val op = Node(null,listOf(Assigment(VirtualRegister(1), BinaryOperation(
            Constant(2), Constant(3), BinaryOperationType.MULTIPLY
        ))))
        val deepOp = Node(Pair(op,Node(null,listOf())),listOf())
        val dValue = BinaryOperation(Constant(2), Constant(3), BinaryOperationType.MULTIPLY)
        assert(placement(deepOp,dValue))
    }
    @Test
    fun fValidation(){
        /*prove that not everything passes*/
        //branch
        val noBranch = Node(Pair(Node(Pair(Node(null,listOf()),null),listOf()),null),listOf())
        assertFalse(branch(noBranch))
        //loop
        val noLoop = Node(Pair(Node(null,listOf()),Node(null,listOf())),listOf())
        assertFalse(loop(noLoop))
        assertFalse(loop(noBranch))
        //calculate
        val value = BinaryOperation(Constant(1), Constant(2),BinaryOperationType.ADD)
        val noValue = Node(null,listOf(BinaryOperation(Constant(1), Constant(1),BinaryOperationType.ADD)))
        assertFalse(calculate(noValue, value))
        //placement
        val prevValue = Node(Pair(
            Node(null,listOf()),
            Node(null,listOf())),
            listOf(BinaryOperation(Constant(1), Constant(1), BinaryOperationType.ADD)))
        assertFalse(placement(prevValue, value))
        val bnValue = Node(Pair(Node(null,listOf()),noValue),listOf())
        assertFalse(placement(bnValue, value))
    }
    @Test
    fun BranchTest(){
        /*
        var a
        if(true){
            a=1
        }else{
            a=2
        }
        */
        var ast = Block(listOf(
            MutableVariableDeclaration("a", IntType),
            Conditional(BoolLiteral(true),
                /*if*/Assignment("a", IntLiteral(1)),
                /*else*/Assignment("a", IntLiteral(2))),
            VariableReference("a")
        ))
        assert(branch(getCFG(ast)))
               /*
        var a
        a=if(true){
            1
        }else{
            2
        }
        */
        ast = Block(listOf(
            MutableVariableDeclaration("a", IntType),
            Assignment("a",
                Conditional(BoolLiteral(true),
                /*if*/IntLiteral(1),
                /*else*/IntLiteral(2))),
            VariableReference("a")
        ))
        assert(branch(getCFG(ast)))
    }
    @Test
    fun loopTest(){
        var ast = Block(listOf(
            MutableVariableDeclaration("a", IntType,IntLiteral(0)),
            Loop(null,Block(listOf(
                Assignment("a", BinaryOperation(VariableReference("a"), BinaryOperator.PLUS, IntLiteral(1)))
            )))
        ))
        assert(loop(getCFG(ast)))
        /*
        a =  while{
            a+1
        }
        */
        ast  = Block(listOf(
            MutableVariableDeclaration("a", IntType,IntLiteral(0)),
            Assignment("a", Loop(null,Block(listOf(
                BinaryOperation(VariableReference("a"), BinaryOperator.PLUS, IntLiteral(1))
            ))))
        ))
        assert(loop(getCFG(ast)))
    }
    @Test
    fun calcTest(){
        var ast:Expr = MutableVariableDeclaration("a",IntType,BinaryOperation(
            IntLiteral(1),BinaryOperator.PLUS,IntLiteral(2)
        ))
        var value = BinaryOperation(Constant(1), Constant(2), BinaryOperationType.ADD)
        assert(calculate(getCFG(ast), value))
        //a=2*(1+1)
        ast = Block(listOf(
            MutableVariableDeclaration("a",IntType),
            Assignment("a", BinaryOperation(
                IntLiteral(2),
                BinaryOperator.MULTIPLY,
                BinaryOperation(
                    IntLiteral(1),
                    BinaryOperator.PLUS,
                    IntLiteral(1)
                )
            )),
        ))
        value  = BinaryOperation(
            Constant(2),
            BinaryOperation(
                Constant(1),
                Constant(1),
                BinaryOperationType.ADD
            ),
            BinaryOperationType.MULTIPLY)
        assert(calculate(getCFG(ast), value))
    }
}
