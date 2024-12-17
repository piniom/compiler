package org.exeval.utilities

class FindUnion(private val size: Int) {
    private val parent = IntArray(size) { it }
    private val rank = IntArray(size) { 0 }

    // Find operation with path compression
    fun find(v: Int): Int {
        if (parent[v] != v) {
            parent[v] = find(parent[v])
        }
        return parent[v]
    }

    // Union operation with union by rank
    fun union(u: Int, v: Int) {
        val rootU = find(u)
        val rootV = find(v)

        if (rootU != rootV) {
            if (rank[rootU] > rank[rootV]) {
                parent[rootV] = rootU
            } else if (rank[rootU] < rank[rootV]) {
                parent[rootU] = rootV
            } else {
                parent[rootV] = rootU
                rank[rootU]++ 
            }
        }
    }
}