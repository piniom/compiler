package org.example.input.interfaces

interface IInput {
    fun nextChar(): Char?
    fun getLocation(): ILocation
    fun setLocation(location: ILocation)
}