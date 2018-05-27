package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.Params

data class CourseLengthScorer(val params: Params) : FeatureScorer {

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        val delta = Math.abs(course.best.distance - params.distance) / Math.max(course.best.distance, params.distance)
        return legs.map{ _ -> delta }
    }
}