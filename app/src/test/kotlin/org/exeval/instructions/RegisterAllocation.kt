package org.exeval.instructions

import io.mockk.mockk
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.cfg.VirtualRegister
import org.exeval.instructions.interfaces.AllocationResult
import org.exeval.instructions.interfaces.LivenessResult

import org.junit.jupiter.api.Test

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
object PhysicalRegisterBank{
    //fuck your sealed class
    private val idMap = mutableMapOf<Int, PhysicalRegister>()
    operator fun invoke(i:Int):PhysicalRegister{
        if(!idMap.containsKey(i)){
            idMap[i] = mockk<PhysicalRegister>()
        }
        return idMap[i]!!
    }
}

class RegisterAllocation{
    data class allocationData(
        val livenessResult: LivenessResult,
        val domain: Set<Register>,
        val range: Set<PhysicalRegister>
    )
    fun getAllocation(data: allocationData):AllocationResult{
        val impl = RegisterAllocatorImpl()
        return impl.allocate(data.livenessResult, data.domain, data.range)
    }
    fun sanity(data : allocationData, a: AllocationResult){
        val spills = a.spills.toSet()

        //mapping,spill disjoint
        assert(spills.intersect(a.mapping.keys).isEmpty())
        //mapping+spill=domain
        assert(data.domain == spills+a.mapping.keys)
        //physical register -> itself
        assert(data.domain.filter{it is PhysicalRegister}.all{a.mapping[it]==it})
        //registers mapped to must exist
        assert(data.range.containsAll(a.mapping.values))
        //no conflict
        assert(data.livenessResult.interference.all{
            val key = it.key
            it.value.all{
                if(key in a.mapping && it in a.mapping){
                    a.mapping[key] != a.mapping[it]
                }else{
                    true
                }
            }
        })
    }
    @Test
    fun spills(){
        //force spills
        val g = mapOf<Register,Set<Register>>(
            VirtualRegisterBank(1) to setOf(VirtualRegisterBank(2),VirtualRegisterBank(3)),
            VirtualRegisterBank(2) to setOf(VirtualRegisterBank(1),VirtualRegisterBank(3)),
            VirtualRegisterBank(3) to setOf(VirtualRegisterBank(1),VirtualRegisterBank(2))
        )
        val data = allocationData(
            LivenessResult(g,mapOf()),
            setOf(VirtualRegisterBank(1),VirtualRegisterBank(2),VirtualRegisterBank(3)),
            setOf(PhysicalRegisterBank(1))
        )
        val a = getAllocation(data)
        sanity(data,a)
        assert(!a.spills.isEmpty())
    }
    @Test
    fun no_spills(){
        //no sills necesarry
        val g = mapOf<Register,Set<Register>>(
            VirtualRegisterBank(1) to setOf(VirtualRegisterBank(2),VirtualRegisterBank(3)),
            VirtualRegisterBank(2) to setOf(VirtualRegisterBank(1),VirtualRegisterBank(3)),
            VirtualRegisterBank(3) to setOf(VirtualRegisterBank(1),VirtualRegisterBank(2))
        )
        val data = allocationData(
            LivenessResult(g,mapOf()),
            setOf(VirtualRegisterBank(1),VirtualRegisterBank(2),VirtualRegisterBank(3)),
            setOf(PhysicalRegisterBank(1), PhysicalRegisterBank(2),PhysicalRegisterBank(3)))
        val a = getAllocation(data)
        sanity(data,a)
        assert(a.spills.isEmpty())
    }
    /*run same program on simulated infinite register machine and allocated physical registries, compare results*/
    class instructions{
        companion object {
            val registerValues = mutableMapOf<Register,Int>().withDefault{0}
        }
        open class base(val origin: Register,val run:()->Unit): Instruction
        class set(origin: Register,val value: Int) : base(origin, {
            registerValues[origin] = value
        })
        class move(origin: Register, val operand: Register) : base(origin,{
            registerValues[origin] = registerValues.getValue(operand)
        })
        class add(origin: Register, val operand: Register) : base(origin,{
            registerValues[origin] = registerValues.getValue(origin)+registerValues.getValue(operand)
        })
        class subtract(origin: Register, val operand: Register) : base(origin,{
            registerValues[origin] = registerValues.getValue(origin)-registerValues.getValue(operand)
        })

    }
    fun simulate(instructionList:List<instructions.base>,a: AllocationResult){
        assert(a.spills.isEmpty())
        val tInstructionsList : List<RegisterAllocation.instructions.base> = instructionList.map{
            when(it){
                is instructions.add -> instructions.add(a.mapping[it.origin]!!,a.mapping[it.operand]!!)
                is instructions.subtract -> instructions.subtract(a.mapping[it.origin]!!,a.mapping[it.operand]!!)
                is instructions.set -> instructions.set(a.mapping[it.origin]!!,it.value)
                is instructions.move -> instructions.move(a.mapping[it.origin]!!,a.mapping[it.operand]!!)
                else -> it
            }
        }
        instructionList.forEach{it.run()}
        val vMap = instructions.registerValues.toMap()
        instructions.registerValues.clear()
        tInstructionsList.forEach{it.run()}
        val pMap = instructions.registerValues.toMap()

        //for all physical registers, there must be a virtual register corresponding to it such that they have the same values
        assert(a.mapping.values.all{
            val pr = it
            a.mapping.keys.any{
                a.mapping[it] == pr && vMap[it]!! == pMap[pr]!!
            }
        })
    }
    @Test
    fun correctness(){
        fun vr(i:Int): VirtualRegister {
            return VirtualRegisterBank(i)
        }
        fun pr(i:Int):PhysicalRegister{
            return PhysicalRegisterBank(i)
        }
        /*
        SET variable1 = 10
        SET variable2 = 5
        SET variable3 = 20
        SET variable4 = 15
        SET variable5 = 30

        variable1 += variable2
        variable4 -= variable3
        variable5 += variable4
        variable2 -= variable1
        variable3 += variable1
        variable4 -= variable5
        variable2 += variable3
        variable3 -= variable1
        */
        val instructionList = listOf(
            instructions.set(vr(1),10),
            instructions.set(vr(2),5),
            instructions.set(vr(3),20),
            instructions.set(vr(4),15),
            instructions.set(vr(5),30),

            instructions.add(vr(1),vr(2)),      //1,2 start
            instructions.subtract(vr(4),vr(3)), //3,4 start
            instructions.move(vr(3),vr(6)),     //3->6!!! end of 3 life, 6 start
            instructions.add(vr(5),vr(4)),      //5 start
            instructions.subtract(vr(2),vr(1)), //
            instructions.move(vr(1),vr(7)),     //1->7!!! end of 1 life, 7 start
            instructions.add(vr(6),vr(7)),      //
            instructions.subtract(vr(4),vr(5)), //end of 4,5 life
            instructions.move(vr(2),vr(8)),     //2->8!!! end of 2 life, 8 start
            instructions.add(vr(8),vr(6)),      //end of 8 life
            instructions.subtract(vr(6),vr(7))  //end of 6,7 life
        )
        val conflictGraph = mapOf<Register,Set<Register>>(
            vr(1) to setOf(vr(2),vr(3),vr(4),vr(5),vr(6)),
            vr(2) to setOf(vr(1),vr(3),vr(4),vr(5),vr(6),vr(7)),
            vr(3) to setOf(vr(1),vr(2),vr(4)),
            vr(4) to setOf(vr(1),vr(2),vr(3),vr(5),vr(6),vr(7)),
            vr(5) to setOf(vr(1),vr(2),vr(3),vr(4),vr(6),vr(7)),
            vr(6) to setOf(vr(1),vr(2),vr(4),vr(5),vr(7),vr(8)),
            vr(7) to setOf(vr(2),vr(4),vr(5),vr(6),vr(8)),
            vr(8) to setOf(vr(6),vr(7))
        )
        val data = allocationData(
            LivenessResult(conflictGraph,mapOf()),
            (1..8).map {vr(it)}.toSet(),
            (1..8).map {pr(it)}.toSet()
        )
        val a = getAllocation(data)
        sanity(data,a)
        simulate(instructionList,a)
    }
    @Test
    fun validate(){
        val i = listOf(
            instructions.set(VirtualRegisterBank(1),10),
            instructions.add(VirtualRegisterBank(1),VirtualRegisterBank(1))
        )
        val a = AllocationResult(
            mapOf(VirtualRegisterBank(1) to PhysicalRegisterBank(1)),
            setOf()
        )
        simulate(i,a)
    }
}