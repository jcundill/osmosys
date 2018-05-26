package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.Params

class RouteChoiceScorer(params: Params) : FeatureScorer {
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.map {
                when {
                    it.hasAlternatives() -> 0.0
                    else -> 1.0
                }
            }

}
