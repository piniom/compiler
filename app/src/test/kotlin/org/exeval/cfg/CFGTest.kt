package org.exeval.cfg

import io.mockk.every
import io.mockk.mockk
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.Tree
import org.exeval.ast.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import org.exeval.cfg.CFGMaker
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.instructions.InstructionCoverer
import org.exeval.instructions.InstructionSetCreator
import org.exeval.instructions.linearizer.Linearizer
import org.junit.Ignore

fun Assigment(r:Register,v:Tree):Tree{
    return AssignmentTree(RegisterTree(r),v)
}

fun Constant(i:Int):Tree{
    return NumericalConstantTree(i.toLong())
}

object VirtualRegisterBank{
    private val idMap = mutableMapOf<Int,VirtualRegister>()
    operator fun invoke(i:Int):VirtualRegister{
        if(idMap.containsKey(i)){
            return idMap[i]!!
        }
        idMap[i] = VirtualRegister()
        return idMap[i]!!
    }
}

class FunctionFrameManagerMock(private val fm: FunctionFrameManager, override val f: FunctionDeclaration): FunctionFrameManager {
    override fun generate_var_access(x: AnyVariable, functionFrameOffset: Tree): AssignableTree {
        return fm.generate_var_access(x, functionFrameOffset)
    }

    override fun generate_function_call(trees: List<Tree>, result: AssignableTree?, then: CFGNode): CFGNode {
        return fm.generate_function_call(trees, result, then)
    }

    override fun variable_to_virtual_register(x: AnyVariable): UsableMemoryCell {
        return fm.variable_to_virtual_register(x)
    }

    override fun generate_prolog(then: CFGNode): CFGNode {
        return CFGNodeImpl(Pair(then, null), listOf())
    }

    override fun generate_epilouge(result: Tree?): CFGNode {
        return CFGNodeImpl(null, listOfNotNull(result))
    }

    override fun alloc_frame_memory(): AssignableTree {
       TODO() 
    }
}

class CFGTest{
    class Node(
        override var branches: Pair<CFGNode,CFGNode?>?,
        override val trees: List<Tree>
    ) : CFGNode {}
    fun getCFG(e:Expr):CFGNode{
        //ASSUMPTION: NO FUNCTION CALLS
        val main = FunctionDeclaration("main",listOf(), NopeType,e)
        val info = AstInfo(main,mapOf())
        val nr = NameResolutionGenerator(info).parse().result
        val tm = TypeChecker(info,nr).parse().result
        val ar = FunctionAnalyser().analyseFunctions(info)
        val ffm = FunctionFrameManagerMock(FunctionFrameManagerImpl(main,ar,mapOf()), main)
        val uag = usageAnalysis(ar.callGraph,nr,main)
        uag.run()
        val ua = uag.getAnalysisResult()
        val maker = CFGMaker(fm=ffm,nameResolution=nr,varUsage=ua,typeMap=tm,mapOf())

        val node = Node(null,listOf())

        return maker.makeCfg(main)
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
        fun recursive(n:CFGNode,ancestors:Set<CFGNode>):Boolean{
            if(n.branches == null){
                return false
            }else if(n.branches!!.second == null){
                if(n.branches!!.first in ancestors){
                    return true
                }
                return recursive(n.branches!!.first,ancestors+n)
            }else{
                if(n.branches!!.first in ancestors || n.branches!!.second!! in ancestors){
                    return true
                }
                return recursive(n.branches!!.first,ancestors+n) || recursive(n.branches!!.second!!,ancestors+n)
            }
        }
        return recursive(cfg,setOf())
    }
    fun calculate(cfg:CFGNode,t:Tree):Boolean{
        //check for simple calculations
        //ASSUMPTION: cfg does not split here
        if(branch(cfg) || loop(cfg)){
            return false
        }

        val assignableContent = mutableMapOf<AssignableTree,Tree>()
        fun recursive(t:Tree):Tree{
            //transform tree by replacing Assignable with Tree in Assignable
            return when(t){
                is AssignableTree -> assignableContent.getValue(t)
                is UnaryOperationTree -> UnaryOperationTree(recursive(t),t.operation)
                is BinaryOperationTree -> BinaryOperationTree(recursive(t.left), recursive(t.right), t.operation)
                else -> t
            }
        }
        fun iterative(){
            var n:CFGNode? = cfg
            while(n != null){
                n.trees.forEach{
                    if(it is AssignmentTree){
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
        if(!branch(cfg)){
            return false
        }
        if(loop(cfg)){
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
            Node(null,listOf(Assigment(VirtualRegisterBank(1),Constant(1)))),
            Node(null,listOf(Assigment(VirtualRegisterBank(1),Constant(2))))
        ),listOf())
        assert(branch(cfg))

        //loop
        var assNode = Node(null,listOf(Assigment(VirtualRegisterBank(1), Constant(0))))
        var loopNode = Node(null,listOf())
        var bodyNode = Node(Pair(loopNode,null),listOf())
        loopNode.branches = Pair(bodyNode,null)
        assNode.branches = Pair(loopNode,null)
        assert(loop(assNode))

        //calculate
        //1<-1+2
        //1
        val value = BinaryOperationTree(Constant(1), Constant(2), BinaryTreeOperationType.ADD)
        val simple = Node(null,listOf(Assigment(VirtualRegisterBank(1), BinaryOperationTree(
            Constant(1), Constant(2), BinaryTreeOperationType.ADD))))
        assert(calculate(simple, value))
        //2 - spread over multiple trees
        val complex = Node(null,listOf(
            Assigment(VirtualRegisterBank(2), Constant(2)),
            Assigment(VirtualRegisterBank(1), BinaryOperationTree(
                Constant(1), RegisterTree(VirtualRegisterBank(2)), BinaryTreeOperationType.ADD))
        ))
        assert(calculate(complex, value))
        //3 - spread over multiple nodes
        val separated1 = Node(null,listOf(Assigment(VirtualRegisterBank(2), Constant(2))))
        val separated2 = Node(null,listOf(Assigment(VirtualRegisterBank(1), BinaryOperationTree(
            Constant(1), RegisterTree(VirtualRegisterBank(2)), BinaryTreeOperationType.ADD))))
        separated1.branches = Pair(separated2,null)
        assert(calculate(separated1, value))

        //placement
        val op = Node(null,listOf(Assigment(VirtualRegisterBank(1), BinaryOperationTree(
            Constant(2), Constant(3), BinaryTreeOperationType.MULTIPLY
        ))))
        val deepOp = Node(Pair(op,Node(null,listOf())),listOf())
        val dValue = BinaryOperationTree(Constant(2), Constant(3), BinaryTreeOperationType.MULTIPLY)
        assert(placement(deepOp,dValue))
    }
    @Test
    fun fValidation(){
        /*prove that tests can fail*/
        //branch
        val noBranch = Node(Pair(Node(Pair(Node(null,listOf()),null),listOf()),null),listOf())
        assertFalse(branch(noBranch))
        //loop
        val noLoop = Node(Pair(Node(null,listOf()),Node(null,listOf())),listOf())
        assertFalse(loop(noLoop))
        assertFalse(loop(noBranch))
        //calculate
        val value = BinaryOperationTree(Constant(1), Constant(2),BinaryTreeOperationType.ADD)
        val noValue = Node(null,listOf(BinaryOperationTree(Constant(1), Constant(1),BinaryTreeOperationType.ADD)))
        assertFalse(calculate(noValue, value))
        //placement
        val prevValue = Node(Pair(
            Node(null,listOf()),
            Node(null,listOf())),
            listOf(BinaryOperationTree(Constant(1), Constant(1), BinaryTreeOperationType.ADD)))
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
        var aVarReference = VariableReference("a")
        var ast = Block(listOf(
            MutableVariableDeclaration("a", IntType),
            Conditional(BoolLiteral(true),
                /*if*/Assignment(aVarReference, IntLiteral(1)),
                /*else*/Assignment(aVarReference, IntLiteral(2))),
                aVarReference
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
        aVarReference = VariableReference("a")
        ast = Block(listOf(
            MutableVariableDeclaration("a", IntType),
            Assignment(aVarReference,
                Conditional(BoolLiteral(true),
                /*if*/IntLiteral(1),
                /*else*/IntLiteral(2))),
            aVarReference
        ))
        assert(branch(getCFG(ast)))
    }
    @Test
    fun loopTest(){
        
        var aVarReference = VariableReference("a")
        var ast = Block(listOf(
            MutableVariableDeclaration("a", IntType,IntLiteral(0)),
            Loop(null,Block(listOf(
                Assignment(aVarReference, BinaryOperation(aVarReference, BinaryOperator.PLUS, IntLiteral(1)))
            )))
        ))
        assert(loop(getCFG(ast)))
        /*
        a =  while{
            a+1
        }
        */
        aVarReference = VariableReference("a")
        ast  = Block(listOf(
            MutableVariableDeclaration("a", IntType,IntLiteral(0)),
            Assignment(aVarReference, Loop(null,Block(listOf(
                BinaryOperation(aVarReference, BinaryOperator.PLUS, IntLiteral(1))
            ))))
        ))
        assert(loop(getCFG(ast)))
        fun Block(vararg e: Expr):Block{
            return Block(listOf(*e))
        }
        ast = Block(
            Loop("1",Block(
                Break(null,IntLiteral(1))
            ))
        )
        assert(loop(getCFG(ast)))
    }
    @Test
    fun calcTest(){
        var ast:Expr = MutableVariableDeclaration("a",IntType,BinaryOperation(
            IntLiteral(1),BinaryOperator.PLUS,IntLiteral(2)
        ))
        var value = BinaryOperationTree(Constant(1), Constant(2), BinaryTreeOperationType.ADD)
        assert(calculate(getCFG(ast), value))
        //a=2*(1+1)
        ast = Block(listOf(
            MutableVariableDeclaration("a",IntType),
            Assignment(VariableReference("a"), BinaryOperation(
                IntLiteral(2),
                BinaryOperator.MULTIPLY,
                BinaryOperation(
                    IntLiteral(1),
                    BinaryOperator.PLUS,
                    IntLiteral(1)
                )
            )),
        ))
        value  = BinaryOperationTree(
            Constant(2),
            BinaryOperationTree(
                Constant(1),
                Constant(1),
                BinaryTreeOperationType.ADD
            ),
            BinaryTreeOperationType.MULTIPLY)
        assert(calculate(getCFG(ast), value))
    }
    @Test
    fun placeTest(){
        val aVarReference = VariableReference("a")
        val ast = Block(listOf(
            MutableVariableDeclaration("a",IntType,IntLiteral(0)),
            Conditional(BoolLiteral(true),
                Assignment(aVarReference, IntLiteral(1)),
                Assignment(aVarReference, IntLiteral(2))
        )))
        val value = Constant(1)
        assert(placement(getCFG(ast), value))
    }
}
