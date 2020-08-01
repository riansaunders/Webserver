package com.botmw

/**
 * NOT designed for cross-thread use.
 * @author bot
 */
class Watch {
    private var start: Long = 0
    fun set() {
        start = System.nanoTime()
    }

    fun elapsed(): Long {
        return System.nanoTime() - start
    }

    fun elapsedMillis(): Long {
        return elapsed() / 1000000
    }

    fun elapsedMillisD(): Double {
        return (System.nanoTime() - start.toDouble()) / 1000000
    }

    fun elapsedSeconds(): Long {
        return elapsed() / 1000000000L
    }

    fun elapsedSecondsD(): Double {
        return (System.nanoTime() - start.toDouble())
    }
}
