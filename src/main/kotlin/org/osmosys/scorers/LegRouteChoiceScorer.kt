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
import java.lang.Double.min
import kotlin.math.min

class LegRouteChoiceScorer(override val weighting: Double = 1.0) : LegScorer {

    /**
     * scores each numbered control based on the route choice available in the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 is to straightforward
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> {
        return routedLegs.map { evaluate(it) }
    }

    private fun evaluate(leg: GHResponse): Double = when {
        leg.hasAlternatives() -> evalAlts(leg)
        else -> 1.0
    }

    private fun evalAlts(leg: GHResponse): Double {
        val sorted = leg.all.map{  it.distance  }.sorted()
        val ratio = (sorted.last() - sorted.first()) / sorted.first()
        val common = leg.all.first().points.intersect(leg.all.last().points).size
        val total = min(leg.all.first().points.size, leg.all.last().points.size)
        val commonRatio = common.toDouble() / total.toDouble();
        return when {
            commonRatio > 0.5 && ratio > 0.5 -> 0.9
            commonRatio > 0.5 && ratio < 0.2 -> 0.75
            else -> ratio
        }
    }

}
