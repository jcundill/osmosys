package jon.test.annealing

import jon.test.rnd
import kotlin.math.exp

class Solver<T : SearchState<T>>(private val problem: Problem<T>, private val scheduler: Scheduler) {
    private val random = rnd

    @Throws(InfeasibleProblemException::class)
    fun solve(): T {
        var currentState = problem.initialState()
        var minState = currentState
        var minEnergy = 1000.0
        var currEnergy = 1000.0
        var steps = 0
        while (true) {
            val temperature = scheduler.getTemperature(steps++)
            if (temperature <= 0.0) {
                return minState
            }
            val nextState = currentState.step()
            val nextEnergy = problem.energy(nextState)
            if (acceptChange(temperature, nextEnergy - currEnergy)) {
                currentState = nextState
                currEnergy = nextEnergy
                if (currEnergy < minEnergy) {
                    minEnergy = currEnergy
                    minState = currentState
                    println("step: $steps, min: $minEnergy")
                }
            }
        }
    }

    /** Always accept changes that decrease energy. Otherwise, use the simulated annealing.  */
    private fun acceptChange(temperature: Double, energyChange: Double): Boolean {
        return when {
            energyChange < 0.0 -> true
            else -> random.nextDouble() <= exp(-1.0 * energyChange / temperature)
        }
    }
}
