package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper

class LegRouteChoiceScorer : FeatureScorer {

    /**
     * scores each numbered control based on the route choice available in the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 is to straightforward
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: PathWrapper): List<Double> {
        return routedLegs.dropLast(1).map { evaluate(it) }
    }

    private fun evaluate(leg: GHResponse): Double = when {
        leg.hasAlternatives() -> evalAlts(leg)
        else -> 1.0
    }

    private fun evalAlts(leg: GHResponse): Double {
        val sorted = leg.all.map{  it.distance  }.sorted()
        return (sorted[1] - sorted[0]) / sorted[0]
    }

}
