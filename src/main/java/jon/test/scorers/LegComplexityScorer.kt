package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class LegComplexityScorer(val params: CourseParameters) : FeatureScorer {

    private val minTurns = 6

    /**
     * scores each numbered control based on the complexity of the route to that control.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too direct
     */
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.dropLast(1).map{ // the finish can't be in the wrong place
                val turns = it.best.instructions.size
                when {
                    turns > minTurns -> 0.0
                    else -> delta(turns, minTurns)
                }
            }

    private fun delta(actual: Number, threshold:Number): Double =
            Math.abs(threshold.toDouble() - actual.toDouble()) / threshold.toDouble()

}
