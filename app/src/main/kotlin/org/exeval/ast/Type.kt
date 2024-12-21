package org.exeval.ast

sealed interface Type{
    fun isNope(): Boolean {
        return this == NopeType
    }
}

data object IntType : Type
data object NopeType : Type
data object BoolType : Type
data class ArrayType(val elementType: Type): Type
