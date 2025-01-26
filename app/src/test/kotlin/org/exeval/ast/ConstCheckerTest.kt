import org.junit.jupiter.api.Test
import org.exeval.ast.ConstChecker
import org.exeval.ast.NameResolution
import org.exeval.ast.IntTypeNode
import org.exeval.ast.AstInfo
import org.exeval.ast.Assignment
import org.exeval.ast.IntLiteral
import org.exeval.ast.ConstantDeclaration
import org.exeval.ast.MutableVariableDeclaration
import org.exeval.utilities.LocationRange
import org.exeval.input.interfaces.Location
import org.exeval.ast.VariableReference

class ConstCheckerTest{
    @Test
    fun assignementToConst(){
        //given
        val constChecker = ConstChecker()
        val ass1 = Assignment(VariableReference("a"), IntLiteral(1)) 
        val dec1 = ConstantDeclaration("a", IntTypeNode, IntLiteral(2))
        val nameResolution = NameResolution(mapOf(), mapOf(), mapOf(), mapOf(), mapOf(Pair(ass1, dec1)), mapOf())
        val loc1: Location = object : Location{ override var line = 1; override var idx = 1} 
        val loc2: Location = object : Location{ override var line = 2; override var idx = 1} 
        val loc3: Location = object : Location{ override var line = 3; override var idx = 1} 
        val loc4: Location = object : Location{ override var line = 4; override var idx = 1} 
        val astInfo = AstInfo(dec1, mapOf(Pair(ass1, LocationRange(loc1, loc2)), Pair(dec1, LocationRange(loc3, loc4))))

        //when
        val errors = constChecker.check(nameResolution, astInfo)

        //then

        assert(errors.isNotEmpty())
    }
    @Test
    fun assignementToMut(){
        //given
        val constChecker = ConstChecker()
        val ass1 = Assignment(VariableReference("a"), IntLiteral(1)) 
        val dec1 = MutableVariableDeclaration("a", IntTypeNode)
        val nameResolution = NameResolution(mapOf(), mapOf(), mapOf(), mapOf(), mapOf(Pair(ass1, dec1)), mapOf())
        val loc1: Location = object : Location{ override var line = 1; override var idx = 1} 
        val loc2: Location = object : Location{ override var line = 2; override var idx = 1} 
        val loc3: Location = object : Location{ override var line = 3; override var idx = 1} 
        val loc4: Location = object : Location{ override var line = 4; override var idx = 1} 
        val astInfo = AstInfo(dec1, mapOf(Pair(ass1, LocationRange(loc1, loc2)), Pair(dec1, LocationRange(loc3, loc4))))

        //when
        val errors = constChecker.check(nameResolution, astInfo)

        //then

        assert(errors.isEmpty())
    }
}