package org.exeval.ast

import org.exeval.utilities.LocationRange

data class AstInfo(
	val root: ASTNode,
	val locations: Map<ASTNode, LocationRange>,
)
