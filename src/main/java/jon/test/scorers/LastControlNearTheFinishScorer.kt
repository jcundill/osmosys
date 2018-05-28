package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

class LastControlNearTheFinishScorer(val params: CourseParameters) : FeatureScorer{
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        val avLegLength = course.best.distance / legs.size
        val lastLegLength = legs.last().best.distance

        return when {
            lastLegLength < avLegLength -> List(legs.size, {0.0}) // all is good
            else -> List(legs.size - 1, {0.0}) + 1.0 // last is bad
        }
    }

}