package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.Params

data class PreviousLegRouteChoiceScorer(val params: Params) : FeatureScorer {
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            listOf(0.0) + legs.dropLast(1).map {
                when {
                    it.hasAlternatives() -> 0.0 //TODO: maybe work out how good the alternatives are
                    else -> 1.0
                }
            }

}
