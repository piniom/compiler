package org.exeval.parser.parser.impls

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf
import org.exeval.parser.grammar.*
import org.exeval.parser.parser.TablesCreator
import org.exeval.utilities.TokenCategories
import java.io.File
import java.net.URL

private val module = SerializersModule {
    polymorphic(Any::class) {
        subclass(Int::class)
        subclass(TypeSymbol::class)
        subclass(VariableDeclarationSymbol::class)
        subclass(ConstantDeclarationSymbol::class)
        subclass(VariableAssignmentSymbol::class)
        subclass(FunctionDeclarationSymbol::class)
        subclass(SimpleFunctionDefinitionSymbol::class)
        subclass(BlockFunctionDefinitionSymbol::class)
        subclass(ForeignFunctionDeclarationSymbol::class)
        subclass(FunctionParamsSymbol::class)
        subclass(FunctionParamSymbol::class)
        subclass(FunctionCallSymbol::class)
        subclass(FunctionCallArgumentsSymbol::class)
        subclass(AllocationSymbol::class)
        subclass(DeallocationSymbol::class)
        subclass(ArrayIndexSymbol::class)
        subclass(ArrayAccessSymbol::class)
        subclass(IfSymbol::class)
        subclass(LoopSymbol::class)
        subclass(BreakSymbol::class)
        subclass(ArithmeticExpressionSymbol::class)
        subclass(SimpleExpressionSymbol::class)
        subclass(ExpressionBlockSymbol::class)
        subclass(ExpressionBlockSymbol.ExpressionChainSymbol::class)
        subclass(ExpressionSymbol::class)
        subclass(ProgramSymbol::class)
        subclass(TopLevelStatementsDeclarationsSymbol::class)
        subclass(TokenCategories::class)
        subclass(EndOfProgramSymbol::class)
        subclass(ValueSymbol::class)
        subclass(VariableReferenceSymbol::class)
        subclass(Operator2ArgSymbol::class)
        subclass(Operator1ArgSymbol::class)
        subclass(StructDefinitionBodyPropertySymbol::class)
        subclass(StructDefinitionBodySymbol::class)
        subclass(StructDefinitionSymbol::class)
        subclass(StructAccessSymbol::class)
        subclass(HereAccess::class)
        subclass(ConstructorDeclarationSymbol::class)
        subclass(StructAccessByArraySymbol::class)
        subclass(StructAccessByIdentyfierNonTypeSymbol::class)
        subclass(StructAccessByFunctionCallSymbol::class)
        subclass(ConstructorDeclarationParamsSymbol::class)
    }
    polymorphic(GrammarSymbol::class) {
        subclass(TypeSymbol::class)
        subclass(VariableDeclarationSymbol::class)
        subclass(ConstantDeclarationSymbol::class)
        subclass(VariableAssignmentSymbol::class)
        subclass(FunctionDeclarationSymbol::class)
        subclass(SimpleFunctionDefinitionSymbol::class)
        subclass(BlockFunctionDefinitionSymbol::class)
        subclass(ForeignFunctionDeclarationSymbol::class)
        subclass(FunctionParamsSymbol::class)
        subclass(FunctionParamSymbol::class)
        subclass(FunctionCallSymbol::class)
        subclass(FunctionCallArgumentsSymbol::class)
        subclass(AllocationSymbol::class)
        subclass(DeallocationSymbol::class)
        subclass(ArrayIndexSymbol::class)
        subclass(ArrayAccessSymbol::class)
        subclass(IfSymbol::class)
        subclass(LoopSymbol::class)
        subclass(BreakSymbol::class)
        subclass(ArithmeticExpressionSymbol::class)
        subclass(SimpleExpressionSymbol::class)
        subclass(ExpressionBlockSymbol::class)
        subclass(ExpressionBlockSymbol.ExpressionChainSymbol::class)
        subclass(ExpressionSymbol::class)
        subclass(ProgramSymbol::class)
        subclass(TopLevelStatementsDeclarationsSymbol::class)

        subclass(StructDefinitionBodyPropertySymbol::class)
        subclass(StructDefinitionBodySymbol::class)
        subclass(StructDefinitionSymbol::class)
        subclass(StructAccessSymbol::class)
        subclass(HereAccess::class)
        subclass(ConstructorDeclarationSymbol::class)
        subclass(StructAccessByArraySymbol::class)
        subclass(StructAccessByIdentyfierNonTypeSymbol::class)
        subclass(StructAccessByFunctionCallSymbol::class)
        subclass(ConstructorDeclarationParamsSymbol::class)
        subclass(TokenCategories::class)
        subclass(EndOfProgramSymbol::class)

        subclass(ValueSymbol::class)
        subclass(VariableReferenceSymbol::class)
        subclass(Operator2ArgSymbol::class)
        subclass(Operator1ArgSymbol::class)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private val format = ProtoBuf { serializersModule = module }

private typealias ActualTables = TablesCreator.Tables<GrammarSymbol, Int>

@OptIn(ExperimentalSerializationApi::class)
class FileTablesCreator(resourcePath: String) : TablesCreator<GrammarSymbol, Int> {
    override val tables: ActualTables

    init {
        val byteArray = javaClass.getResourceAsStream(resourcePath)!!.readBytes()
        tables = format.decodeFromByteArray(byteArray)
    }

    companion object {
        fun saveTablesToFile(tables: ActualTables, resourcePath: String) {
            val byteArray = format.encodeToByteArray(tables)
            val resource: URL = this::class.java.getResource(resourcePath)!!
            println(resource)
            val filePath = resource.path
            println(filePath)
            val file = File(filePath)
            file.writeBytes(byteArray)
        }
    }
}