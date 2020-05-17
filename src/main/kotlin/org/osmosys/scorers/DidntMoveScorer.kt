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
import com.graphhopper.util.DistancePlaneProjection

class DidntMoveScorer(override val weighting: Double = 1.0) : LegScorer {
    private val dist2d = DistancePlaneProjection()
    private val minMoveDistance: Double = 50.0

    /**
     * scores each numbered control based on the length of the previous leg.
     * i.e. control 2 is in a bad place as it in the same place as 1
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> =
            listOf(0.0) + routedLegs.windowed(2, 1, false).map { ls ->
                if (isSameStartPoint(ls.first(), ls.last())) 1.0 else 0.0
            }

    private fun isSameStartPoint(first: GHResponse, second: GHResponse): Boolean {
        val xs = first.best.points
        val ys = second.best.points
        val dist = dist2d.calcDist(xs.getLat(0), xs.getLon(0), ys.getLat(0), ys.getLon(0))
        return dist < minMoveDistance
    }

}