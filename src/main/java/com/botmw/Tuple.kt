package com.botmw

class Tuple<A, B>(val left: A, val right: B) {
    override fun toString(): String {
        return "<$left,$right>"
    }

}
