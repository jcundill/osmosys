package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class LegRouteChoiceScorer(val params: CourseParameters) : FeatureScorer {

    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> {
        return scorePrevious(routedLegs).zip(scoreFollowing(routedLegs)).map { it.first + it.second }
    }

    /**
     * scores each numbered control based on the route choice available on the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too straightforward
     */

    fun scorePrevious(routedLegs: List<GHResponse>): List<Double> =
            routedLegs.dropLast(1).map { evaluate(it) }

    /**
     * scores each numbered control based on the route choice available in the following leg.
     * i.e. control 2 is in a bad place as the route from 2 to 3 is to straightforward
     */
    fun scoreFollowing(routedLegs: List<GHResponse>): List<Double> =
            routedLegs.drop(1).map { evaluate(it)}

    fun evaluate(leg: GHResponse): Double = when {
                leg.hasAlternatives() -> 0.0 //TODO: maybe work out how good the alternatives are
                else -> 1.0
            }

}
