package org.exeval.ast

data class FunctionAnalysisResult(
    val callGraph: MutableMap<AnyCallableDeclaration, MutableSet<AnyCallableDeclaration>>,
    val staticParents: Map<AnyCallableDeclaration, AnyCallableDeclaration?>,
    val variableMap: Map<AnyVariable, AnyCallableDeclaration>,
    val isUsedInNested: Map<AnyVariable, Boolean>
) {
    fun maxNestedFunctionDepth(): Int {
        val depths = mutableMapOf<AnyCallableDeclaration, Int>()
        for (function in staticParents.keys) {
            calculateFunctionDepth(function, depths)
        }
        return depths.maxOf { it.value }
    }

    private fun calculateFunctionDepth(function: AnyCallableDeclaration, depths: MutableMap<AnyCallableDeclaration, Int>) {
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
