package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import jon.test.Params

class CourseLengthScorer(val params: Params) : FeatureScorer {

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.map{ _ -> Math.abs(course.best.distance - params.distance) / params.distance }
}