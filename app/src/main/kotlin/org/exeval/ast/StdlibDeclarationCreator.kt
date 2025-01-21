import org.exeval.ast.AnyFunctionDeclaration
import org.exeval.ast.ForeignFunctionDeclaration
import org.exeval.ast.Parameter
import org.exeval.ast.IntType
import org.exeval.ast.NopeType

class StdlibDeclarationsCreator {
    companion object {
        fun getDeclarations(): List<AnyFunctionDeclaration> {
            return listOf(
                ForeignFunctionDeclaration(
                    name = "print_int",
                    parameters = listOf(Parameter("a", IntType)),
                    returnType = NopeType
                ),
                ForeignFunctionDeclaration(
                    name = "scan_int",
                    parameters = listOf(),
                    returnType = IntType
                ),
                ForeignFunctionDeclaration(
                    name = "malloc",
                    parameters = listOf(Parameter("size", IntType)),
                    returnType = IntType
                ),
                ForeignFunctionDeclaration(
                    name = "free",
                    parameters = listOf(Parameter("pointer", IntType)),
                    returnType = NopeType
                )
            )
        }
    }
}

