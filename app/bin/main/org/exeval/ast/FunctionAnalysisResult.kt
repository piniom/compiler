package org.exeval.ast

data class FunctionAnalysisResult(
    val callGraph: CallGraph,
    val staticParents: Map<FunctionDeclaration, FunctionDeclaration?>,
    val variableMap: Map<AnyVariable, FunctionDeclaration>,
    val isUsedInNested: Map<AnyVariable, Boolean>
)
