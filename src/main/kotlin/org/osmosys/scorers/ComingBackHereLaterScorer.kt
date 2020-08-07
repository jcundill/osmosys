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
import com.graphhopper.util.shapes.GHPoint
import org.osmosys.improvers.dist

class ComingBackHereLaterScorer(override val weighting: Double = 1.0) : LegScorer {

    /**
     * works out if we run through a future control on this leg
     * and scores it badly if we do
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> {
        return listOf(0.0) + routedLegs.mapIndexed { idx, leg ->
            val futureLegs = routedLegs.subList(idx + 1, routedLegs.size)
            evaluate(futureLegs, leg)
        }.drop(1)
    }

    private fun evaluate(futureLegs: List<GHResponse>, thisLeg: GHResponse): Double {
        return when {
            futureLegs.isEmpty() -> 0.0 // no further legs
            else -> {
                val remainingControls = futureLegs.map { it.best.points.last() }
                when {
                    thisLeg.best.points.any { goesTooCloseToAFutureControl(remainingControls, it) } -> 1.0
                    else -> 0.0
                }
            }
        }
    }

    private fun goesTooCloseToAFutureControl(ctrls: List<GHPoint>, p: GHPoint) =
            ctrls.any { c -> dist(p, c) < 50.0 && dist(p, c) > 5.0}
}