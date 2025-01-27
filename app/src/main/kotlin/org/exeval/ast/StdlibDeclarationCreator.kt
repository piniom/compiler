import org.exeval.ast.AnyFunctionDeclaration
import org.exeval.ast.ForeignFunctionDeclaration
import org.exeval.ast.Parameter
import org.exeval.ast.IntType
import org.exeval.ast.NopeType

class StdlibDeclarationsCreator {
    companion object {
        fun getDeclarations(): List<ForeignFunctionDeclaration> {
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
                )
            )
        }
    }
}

