
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
}
