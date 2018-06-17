package jon.test.annealing

class LinearDecayScheduler(private val initialTemperature: Double, private val totalSteps: Int) : Scheduler {

    override fun getTemperature(steps: Int): Double =
            (1 - steps.toDouble() / totalSteps) * initialTemperature
}
