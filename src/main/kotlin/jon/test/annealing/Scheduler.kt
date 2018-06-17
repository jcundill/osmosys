package jon.test.annealing

interface Scheduler {
    fun getTemperature(steps: Int): Double
}
