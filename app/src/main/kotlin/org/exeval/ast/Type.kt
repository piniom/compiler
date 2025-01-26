package org.exeval.ast

sealed interface Type {
    fun isNope(): Boolean {
        return this == NopeType
    }
}

data object IntType : Type
data object NopeType : Type
data object BoolType : Type
data object NothingType : Type
data class ArrayType(val elementType: Type): Type

data class StructType(
    val fields: Map<String /* name */, Field>,
    val size: Long // The real size it uses in memory
): Type
data class Field(
    val type: Type,
    val offset: Long
)
