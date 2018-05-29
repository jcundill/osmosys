package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class FollowingLegRouteChoiceScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * scores each numbered control based on the route choice available in the following leg.
     * i.e. control 2 is in a bad place as the route from 2 to 3 is to straightforward
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> =
            routedLegs.drop(1).map { // the start can't be in the wrong place
                when {
                    it.hasAlternatives() -> 0.0 //TODO: maybe work out how good the alternatives are
                    else -> 1.0
                }
            }

}
