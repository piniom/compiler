package org.exeval.ast

data class FunctionAnalysisResult(
    val callGraph: CallGraph,
    val staticParents: Map<FunctionDeclaration, FunctionDeclaration?>,
    val variableMap: Map<AnyVariable, FunctionDeclaration>,
    val isUsedInNested: Map<AnyVariable, Boolean>
) {
    fun maxNestedFunctionDepth(): kotlin.Int {
        val depths = mutableMapOf<FunctionDeclaration, kotlin.Int>()
        for (function in staticParents.keys) {
            calculateFunctionDepth(function, depths)
        }
        return depths.maxOf { it.value }
    }

    private fun calculateFunctionDepth(function: FunctionDeclaration, depths: MutableMap<FunctionDeclaration, kotlin.Int>) {
        if (depths[function] != null) {
            return
        }
        val parent = staticParents[function]
        if (parent == null) {
            depths[function] = 0
        } else {
            calculateFunctionDepth(parent, depths)
            depths[function] = depths[parent]!! + 1
        }
    }
}
