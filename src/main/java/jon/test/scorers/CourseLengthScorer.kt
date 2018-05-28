package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.Params

data class CourseLengthScorer(val params: Params) : FeatureScorer {

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        val distance = course.best.distance
        val delta = when {
            distance < params.minAllowedDistance -> 1.0
            distance > params.maxAllowedDistance -> 1.0
            else -> 0.0
        }
        return legs.map{ _ -> delta }
    }
}