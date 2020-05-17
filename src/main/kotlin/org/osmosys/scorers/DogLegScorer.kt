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

package org.osmosys.scorers

import com.graphhopper.GHResponse
import kotlin.math.min

class DogLegScorer(override val weighting: Double = 1.0) : LegScorer {

    /**
     * scores each numbered control based on the repetition of the route to it and the route from the previous control.
     * i.e. control 3 is in a bad place as the route from 1 to 2 is pretty much the same as the route from 2 to 3
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> {
        return dogLegs(routedLegs.map { it.best.points })
    }

    fun <T> dogLegs(routes: List<Iterable<T>>): List<Double> =
            when {
                routes.size < 2 -> emptyList()
                else -> listOf(0.0) + routes.windowed(2).map { dogLegScore(it.first().toList(), it.last().toList()) }
            }


    fun <T> dogLegScore(a2b: List<T>, b2c: List<T>): Double {
        if (a2b.size < 2 || b2c.size < 2) return 1.0 //controls are in the same place
        val numInAandB = a2b.dropLast(1).filter { b2c.drop(1).contains(it) }.size
        return numInAandB.toDouble() / (min(b2c.size, a2b.size).toDouble() - 1.0)
    }
}