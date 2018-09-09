package jon.test

import java.util.*

interface RandomStream {

    fun nextDouble(): Double
}

class PseudoRandom : RandomStream {
    private val rnd = Random(System.currentTimeMillis())
    private val doubleStream = rnd.doubles().iterator()

    override fun nextDouble(): Double = doubleStream.next()
}

class RepeatableRandom(seed: Long) : RandomStream {
    private val rnd = Random(seed)
    private val doubleStream = rnd.doubles().iterator()

    override fun nextDouble(): Double = doubleStream.next()
}

object PredictableRandom : RandomStream {
    private val doubles = arrayOf(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9)
    private val seq = generateSequence(0) { (it + 1) % doubles.size }
            .map { doubles[it] }.iterator()

    override fun nextDouble(): Double = seq.next()
}