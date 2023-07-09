package dev.flammky.valorantcompanion.base

// TODO: should we constraint that the [block] must return [Nothing] ?
@Suppress("FunctionName")
inline fun Loop(
    block: LoopScope.() -> Unit
): Unit = (LoopScope() as RealLoop).loop(block)

// block must return nothing, that includes call to `breakLoop` or `continueLoop`
@Suppress("FunctionName")
inline fun StrictLoop(
    block: LoopScope.() -> Nothing
): Unit = Loop(block)

fun LoopScope(): LoopScope = RealLoop()

interface LoopScope {

    fun LOOP_BREAK(): Nothing

    fun LOOP_CONTINUE(): Nothing
}

// public so we can inline the block
class RealLoop internal constructor(): LoopScope {

    inline fun loop(block: LoopScope.() -> Unit): Unit {
        while (true) {
            try {
                block()
            } catch (ex: Exception) {
                if (shouldBreak(ex)) break
                if (shouldContinue(ex)) continue
                throw ex
            }
        }
    }

    override fun LOOP_BREAK(): Nothing {
        throw BreakLoopException()
    }

    override fun LOOP_CONTINUE(): Nothing {
        throw ContinueLoopException()
    }

    fun shouldBreak(exception: Exception) = exception is BreakLoopException

    fun shouldContinue(exception: Exception) = exception is ContinueLoopException
}

// private ?

internal class BreakLoopException : Exception()

internal class ContinueLoopException : Exception()
