package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class PreviousLegRouteChoiceScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * scores each numbered control based on the route choice available on the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too straightforward
     */

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.dropLast(1).map { // the finish can't be in the wrong place
                when {
                    it.hasAlternatives() -> 0.0 //TODO: maybe work out how good the alternatives are
                    else -> 1.0
                }
            }

}
