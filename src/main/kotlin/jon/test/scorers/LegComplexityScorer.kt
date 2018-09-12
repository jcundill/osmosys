package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.Instruction

class LegComplexityScorer : FeatureScorer {


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
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> =
            routedLegs.dropLast(1).map { evaluate(it) } // the finish can't be in the wrong place


    private fun evaluate(leg: GHResponse): Double {
        val instructions = leg.best.instructions

        val num = instructions.size.toDouble()
        val turns = instructions.filter { turnInstructions.contains(it.sign) }.size.toDouble()

        return 1.0 - (turns / num)
    }
}
