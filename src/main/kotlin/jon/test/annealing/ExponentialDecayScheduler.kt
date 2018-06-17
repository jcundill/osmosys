package jon.test.annealing


class ExponentialDecayScheduler(private val initialTemperature: Double, totalSteps: Int) : Scheduler {
    private val decayRate: Double = Math.log(EPSILON / initialTemperature) / totalSteps

    override fun getTemperature(steps: Int): Double {
        val temperature = initialTemperature * Math.exp(decayRate * steps)
        return when {
            temperature < EPSILON -> 0.0
            else -> temperature
        }
    }

    companion object {
        internal const val EPSILON = 0.001
    }
}
