package org.exeval.ast

import AnyVariable
import FunctionDeclaration

data class FunctionAnalysisResult(
    val callGraph: CallGraph,
    val staticParents: Map<FunctionDeclaration, FunctionDeclaration?>,
    val variableMap: Map<AnyVariable, FunctionDeclaration>,
    val isUsedInNested: Map<AnyVariable, Boolean>
)
