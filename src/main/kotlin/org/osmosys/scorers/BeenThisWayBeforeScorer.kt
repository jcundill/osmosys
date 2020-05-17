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

class BeenThisWayBeforeScorer(override val weighting: Double = 1.0) : LegScorer {

    /**
     * works out how much of the leg to this numbered control has been travelled along
     * already.
     * Find the worst duplication in any of the proceeding legs and return that as the score
     * Do not include previous leg - that is captured in the Dog Leg Scorer
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> =
            routedLegs.mapIndexed { idx, leg -> evaluate(routedLegs.subList(0, idx), leg) }

    private fun evaluate(previousLegs: List<GHResponse>, thisLeg: GHResponse): Double {
        return when {
            previousLegs.size < 2 -> 0.0 // no legs other than the previous
            else -> {
                previousLegs.map { l -> compareLegs(l, thisLeg) }.max()!!
            }
        }
    }

    private fun compareLegs(a: GHResponse, b: GHResponse): Double {
        val pointsA = a.best.points.drop(1).dropLast(1)
        val pointsB = b.best.points.drop(1).dropLast(1)
        return pointsB.intersect(pointsA).size.toDouble() / min(pointsB.size.toDouble(), pointsA.size.toDouble())
    }
}
