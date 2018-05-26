package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.Params

class BeenThisWayBeforeScorer(params: Params): FeatureScorer {
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        val points = course.best.points
        val distinct = points.distinct()
        return legs.map {_ -> (points.size.toDouble() - distinct.size.toDouble()) / points.size.toDouble()}
    }
}
