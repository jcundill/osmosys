package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class LegComplexityScorer(val params: CourseParameters) : FeatureScorer {

    private val minTurns = 6

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.map{
                val turns = it.best.instructions.size
                when {
                    turns > minTurns -> 0.0
                    else -> delta(turns, minTurns)
                }
            }

    private fun delta(actual: Number, threshold:Number): Double =
            Math.abs(threshold.toDouble() - actual.toDouble()) / threshold.toDouble()

}
