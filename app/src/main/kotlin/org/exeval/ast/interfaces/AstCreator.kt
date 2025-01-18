package org.exeval.ast.interfaces

import org.exeval.ast.AstInfo
import org.exeval.input.interfaces.Input
import org.exeval.parser.interfaces.ParseTree

interface AstCreator<S> {
	fun create(
		parseTree: ParseTree<S>,
		input: Input,
	): AstInfo
}
