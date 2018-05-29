package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters


data class FollowingLegLengthScorer(val params: CourseParameters): FeatureScorer{

    private val minAllowed = 20.0
    private val maxAllowed = 500.0  // third of the course on a single leg

    /**
     * scores each numbered control based on the length of the coming leg.
     * i.e. control 2 is in a bad place as the route from 2 to 3 was too long
     */
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {

        return legs.drop(1).map { // the start can't be in the wrong place
            val best = it.best.distance
            when {
                best < minAllowed -> 1.0
                best > maxAllowed -> 1.0
                else -> 0.0
            }}
    }

}
