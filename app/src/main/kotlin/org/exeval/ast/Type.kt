package org.exeval.ast

sealed interface Type{
    public fun isNope(): Boolean {
        return this == NopeType
    }
}

data object IntType : Type
data object NopeType : Type
data object BoolType : Type
