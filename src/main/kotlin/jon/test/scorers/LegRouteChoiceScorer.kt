package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class LegRouteChoiceScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * scores each numbered control based on the route choice available in the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 is to straightforward
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> {
        return routedLegs.dropLast(1).map { evaluate(it) }
    }

    fun evaluate(leg: GHResponse): Double = when {
                leg.hasAlternatives() -> 0.0 //TODO: maybe work out how good the alternatives are
                else -> 1.0
            }

}
