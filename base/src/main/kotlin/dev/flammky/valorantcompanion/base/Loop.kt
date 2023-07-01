package dev.flammky.valorantcompanion.base

inline fun loop(
    block: LoopBuilder.() -> Unit
) {
    (LoopBuilder() as RealLoopBuilder).invoke(block)
}

fun LoopBuilder(): LoopBuilder = RealLoopBuilder()

interface LoopBuilder {

    fun breakLoop(): Nothing

    fun continueLoop(): Nothing
}

class RealLoopBuilder internal constructor(): LoopBuilder {

    var breakInvoked = false
        private set

    var continueInvoked = false
        private set

    inline fun invoke(block: LoopBuilder.() -> Unit) {
        while (true) {
            try {
                block()
            } catch (ex: Exception) {
                if (ex !is BreakLoopException && ex !is ContinueLoopException) throw ex
            }
            if (breakInvoked) break
            if (continueInvoked) continue
        }
    }

    override fun breakLoop(): Nothing {
        breakInvoked = true
        throw BreakLoopException()
    }

    override fun continueLoop(): Nothing {
        continueInvoked = true
        throw ContinueLoopException()
    }

}
class BreakLoopException() : Exception()

class ContinueLoopException() : Exception()