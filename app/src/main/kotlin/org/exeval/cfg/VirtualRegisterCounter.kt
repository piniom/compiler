package org.exeval.cfg


// There should be only one counter for the whole program to ensure uniqueness
class VirtualRegisterCounter() {
    private var count = 100;
    public fun next(): Int {
        return count++
    }
}

