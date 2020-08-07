/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.osmosys.annealing

import org.osmosys.rnd
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
            if (acceptChange(temperature, nextEnergy, currEnergy)) {
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
    private fun acceptChange(temperature: Double, nextEnergy: Double, currentEnergy: Double): Boolean {
        return when {
            currentEnergy > nextEnergy  -> true
            else -> random.nextDouble() <= exp( (currentEnergy - nextEnergy) / temperature)
        }
    }
}
