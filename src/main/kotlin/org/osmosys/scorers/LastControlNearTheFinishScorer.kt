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

class LastControlNearTheFinishScorer(override val weighting: Double = 1.0) : LegScorer {

    /**
     * scores the last numbered control on its distance from the finish.
     * i.e. control 10 is in a bad place as it is the last control and it is 4k from the finish
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> {
        val avLegLength = routedLegs.sumByDouble { it.best.distance }/ routedLegs.size
        val lastLegLength = routedLegs.last().best.distance

        return List(routedLegs.size - 1) { 0.0 } + when {
            lastLegLength < 50.0 -> 1.0 // way too short
            lastLegLength < avLegLength / 2 -> 0.0 // all is good
            else -> 1.0 // last is bad
        }
    }

}