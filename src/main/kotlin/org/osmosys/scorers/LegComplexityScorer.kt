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
import com.graphhopper.util.Instruction

class LegComplexityScorer(override val weighting: Double = 1.0) : LegScorer {

    private val turnInstructions = listOf(
            Instruction.TURN_LEFT,
            Instruction.TURN_RIGHT,
            Instruction.TURN_SHARP_LEFT,
            Instruction.TURN_SHARP_RIGHT,
            Instruction.TURN_SLIGHT_LEFT,
            Instruction.TURN_SLIGHT_RIGHT,
            Instruction.U_TURN_LEFT,
            Instruction.U_TURN_RIGHT,
            Instruction.U_TURN_UNKNOWN,
            Instruction.LEAVE_ROUNDABOUT
    )

    /**
     * scores each numbered control based on the complexity of the route to that control.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too direct
     */
    override fun score(routedLegs: List<GHResponse>): List<Double> =
            routedLegs.map { evaluate(it) }


    private fun evaluate(leg: GHResponse): Double {
        val instructions = leg.best.instructions

        val turns = instructions.filter { turnInstructions.contains(it.sign) }.size
        val turnDensity = 1000.0 * turns / leg.best.distance   // turns per K

        return when {
            turnDensity > 4.0 -> 0.0
            else -> 1.0 //- (turns.toDouble() / num.toDouble())
        }
    }
}
