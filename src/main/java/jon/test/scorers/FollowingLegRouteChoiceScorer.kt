package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class FollowingLegRouteChoiceScorer(val params: CourseParameters) : FeatureScorer {
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.map {
                when {
                    it.hasAlternatives() -> 0.0 //TODO: maybe work out how good the alternatives are
                    else -> 1.0
                }
            }

}
