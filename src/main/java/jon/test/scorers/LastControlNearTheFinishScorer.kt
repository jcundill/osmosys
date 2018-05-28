package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

class LastControlNearTheFinishScorer(val params: CourseParameters) : FeatureScorer{

    /**
     * scores the last numbered control on its distance from the finish.
     * i.e. control 10 is in a bad place as it is the last control and it is 4k from the finish
     */
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        val avLegLength = course.best.distance / legs.size
        val lastLegLength = legs.last().best.distance

        return List(legs.size - 2, {0.0}) + when {
            lastLegLength < avLegLength / 2 -> 0.0 // all is good
            else ->  1.0 // last is bad
        }
    }

}