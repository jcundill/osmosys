package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class LegLengthScorer(val params: CourseParameters): FeatureScorer {
    private val minAllowed = 20.0
    private val maxAllowed = 500.0  // third of the course on a single leg


    /**
     * scores each numbered control based on the length of the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too long
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> {
        return routedLegs.dropLast(1).map { evaluate(it) }//.zip(scoreFollowing(routedLegs)).map { (it.first + it.second) / 2.0 }
    }


    private fun evaluate(leg: GHResponse): Double {
        val best = leg.best.distance
        return when {
            best < minAllowed -> 1.0
            best > maxAllowed -> 1.0
            else -> 0.0
        }
    }
}
