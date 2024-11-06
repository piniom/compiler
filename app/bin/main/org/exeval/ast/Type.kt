package org.exeval.ast

sealed interface Type

data object IntType : Type
data object NopeType : Type
data object BoolType : Type
