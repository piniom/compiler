
import org.exeval.instructions.*
import org.exeval.cfg.Register
import org.exeval.cfg.VirtualRegister
import org.exeval.cfg.PhysicalRegister
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IntructionTest {

    @Test
    fun `test instructions classes`() {
        val reg = VirtualRegister()
        val addInstr = AddInstruction(PhysicalRegister.RAX, reg)
        assertEquals(addInstr.isCopy(), false)
        assertEquals(addInstr.usedRegisters(), listOf(PhysicalRegister.RAX, reg))
        assertEquals(addInstr.definedRegisters(), listOf(PhysicalRegister.RAX))
        val mapping: Map<Register, PhysicalRegister> = mapOf(reg to PhysicalRegister.R10, PhysicalRegister.RAX to PhysicalRegister.RAX)
        assertEquals(addInstr.toAsm(mapping), "ADD RAX, R10")
    }

    @Test
    fun `test delayed constant extraction`() {
        val reg = VirtualRegister()
        val delayedConstant = DelayedNumericalConstant({ 5 })
        val subInstr = SubInstruction(reg, delayedConstant)
        assertEquals(subInstr.isCopy(), false)
        assertEquals(subInstr.usedRegisters(), listOf(reg))
        assertEquals(subInstr.definedRegisters(), listOf(reg))
        val mapping: Map<Register, PhysicalRegister> = mapOf(reg to PhysicalRegister.R8)
        assertEquals(subInstr.toAsm(mapping), "SUB R8, 5")
    }

    @Test
    fun `mov isn't copy`() {
        val reg = VirtualRegister()
        val constant = NumericalConstant(7)
        val movInstr = MovInstruction(reg, constant)
        assertEquals(movInstr.isCopy(), false)
        assertEquals(movInstr.usedRegisters(), emptyList<Register>())
        assertEquals(movInstr.definedRegisters(), listOf(reg))
        val mapping: Map<Register, PhysicalRegister> = mapOf(reg to PhysicalRegister.R9)
        assertEquals(movInstr.toAsm(mapping), "MOV R9, 7")
    }

    @Test
    fun `mov is copy`() {
        val reg1 = VirtualRegister()
        val reg2 = VirtualRegister()
        val movInstr = MovInstruction(reg1, reg2)
        assertEquals(movInstr.isCopy(), true)
        assertEquals(movInstr.usedRegisters(), listOf(reg2))
        assertEquals(movInstr.definedRegisters(), listOf(reg1))
        val mapping: Map<Register, PhysicalRegister> = mapOf(
            reg1 to PhysicalRegister.RDX,
            reg2 to PhysicalRegister.RCX
        )
        assertEquals(movInstr.toAsm(mapping), "MOV RDX, RCX")
    }
}
