package jon.test.annealing

class UpInMiddleScheduler(private val initialTemperature: Double, private val totalSteps: Int): Scheduler {
    override fun getTemperature(steps: Int): Double = when {
        steps < totalSteps ->  initialTemperature
        else -> 0.0
    }
}