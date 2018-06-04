package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class LegComplexityScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * scores each numbered control based on the complexity of the route to that control.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too direct
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> =
        // the finish can't be in the wrong place
        routedLegs.dropLast(1).map{ evaluate(it) }


    private fun evaluate(leg: GHResponse): Double {
        val turns = leg.best.instructions.size
        val length = leg.best.distance

        // we want on average a turn for every 100m travelled
        return when {
            turns == 0 -> 1.0
            length == 0.0 -> 1.0
            else -> 1.0 - turns / length / 100.0

        }
    }

}
