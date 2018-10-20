package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper

class LegLengthScorer() : FeatureScorer {

    private val minLegLength = 20.0

    /**
     * scores each numbered control based on the length of the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too long
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: PathWrapper): List<Double> {
        val maxLegLength = 2.0 * routedCourse.distance / routedLegs.size

        return routedLegs.dropLast(1).map { evaluate(it, maxLegLength) }//.zip(scoreFollowing(routedLegs)).map { (it.first + it.second) / 2.0 }
    }


    private fun evaluate(leg: GHResponse, maxLegLength: Double): Double {
        val best = leg.best.distance
        return when {
            best < minLegLength -> 1.0
            best > maxLegLength -> 1.0
            else -> 0.0
        }
    }
}
