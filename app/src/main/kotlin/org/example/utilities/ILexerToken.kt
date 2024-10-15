package org.example.utilities

import org.example.input.interfaces.ILocation
import org.example.utilities.ITokenCategory

interface ILexerToken {
    val categories: Set<ITokenCategory>
    val text: String
    val startLocation: ILocation
    val stopLocation: ILocation
}