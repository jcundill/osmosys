package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper

class LastControlNearTheFinishScorer : FeatureScorer {

    /**
     * scores the last numbered control on its distance from the finish.
     * i.e. control 10 is in a bad place as it is the last control and it is 4k from the finish
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: PathWrapper): List<Double> {
        val avLegLength = routedCourse.distance / routedLegs.size
        val lastLegLength = routedLegs.last().best.distance

        return List(routedLegs.size - 2) { 0.0 } + when {
            lastLegLength < 50.0 -> 1.0 // way too short
            lastLegLength < avLegLength / 2 -> 0.0 // all is good
            else -> 1.0 // last is bad
        }
    }

}