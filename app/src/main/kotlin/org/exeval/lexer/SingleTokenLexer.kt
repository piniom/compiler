package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location
import org.exeval.utilities.SimpleLexerToken
import org.exeval.utilities.diagnostics.TextDidNotMatchAnyTokensDiagnostics
import org.exeval.utilities.interfaces.LexerToken
import org.exeval.utilities.interfaces.OperationResult
import org.exeval.utilities.interfaces.TokenCategory
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.collections.listOf

class SingleTokenLexer(
	dfas: Map<DFA<*>, TokenCategory>,
	input: Input,
) {
	private val activeWalkers: MutableMap<DFAWalker<*>, TokenCategory> =
		dfas.mapKeys { (dfa, _) -> DFAWalker(input.location, dfa) }.toMutableMap()
	private var accepted: MutableMap<DFAWalker<*>, TokenCategory>
	private val input: Input
	private val start: Location

	private val text: StringBuilder = StringBuilder()

	init {
		accepted = mutableMapOf()
		this.input = input
		this.start = input.location
	}

	public fun run(): OperationResult<LexerToken?> {
		while (activeWalkers.isNotEmpty()) {
			val char = this.input.nextChar() ?: break
			val walkersToDeactivate = mutableListOf<DFAWalker<*>>() // Create a list to store walkers to deactivate

			for ((walker, category) in activeWalkers) {
				if (!walker.transition(char, input.location) || walker.isDead()) {
					walkersToDeactivate.add(walker) // Add the walker to the list
				}
			}
			deactivateWalkers(walkersToDeactivate)
			this.text.append(char)
		}
		deactivateWalkers(activeWalkers.keys.toList())
		val (walker, _) =
			accepted.entries.firstOrNull() ?: return OperationResult(
				null,
				listOf(TextDidNotMatchAnyTokensDiagnostics.create(text.toString(), this.start, this.input.location)),
			)
		val acceptedLoc = walker.maxAccepting!!
		this.input.location = acceptedLoc

		return OperationResult(
			SimpleLexerToken(
				accepted.values.toSet(),
				text.substring(0, walker.maxAcceptingCount),
				this.start,
				acceptedLoc,
			),
			listOf(),
		)
	}

	private fun deactivateWalkers(walkers: List<DFAWalker<*>>) {
		for (walker in walkers) {
			this.deactivateWalker(walker)
		}
	}

	private fun deactivateWalker(walker: DFAWalker<*>) {
		val category = this.activeWalkers.remove(walker) ?: throw Exception("Walker not present!")
		if (walker.maxAccepting == null) return
		val any = accepted.entries.firstOrNull()
		if (any == null || walker.compareTo(any.key) == 0) {
			accepted[walker] = category
			return
		}
		if (walker.compareTo(any.key) < 0) return
		this.accepted = mutableMapOf(walker to category)
	}
}
