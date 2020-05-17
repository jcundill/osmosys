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

class OnlyGoToTheFinishAtTheEndScorer(override val weighting: Double = 1.0) : LegScorer {

    /**
     * works out if we run near the finish control on this leg
     * and scores it badly if we do
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> {
        val finish = routedLegs.last().best.points.last()

        // ignore the start and the finish being near the finish - evaluate the rest
        return listOf(0.0) + routedLegs.drop(1).dropLast(1).map { leg ->
            evaluate(leg, finish)
        } + 0.0
    }

    private fun evaluate(thisLeg: GHResponse, finish: GHPoint): Double = when {
        thisLeg.best.points.any { dist(it, finish) < 150.0 } -> 1.0
        else -> 0.0
    }

}