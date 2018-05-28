package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters


data class FollowingLegLengthScorer(val params: CourseParameters): FeatureScorer{

    private val minAllowed = 20.0
    private val maxAllowed = params.distance / 3.0  // third of the course on a single leg

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        val firstScore = 0.0 // we don't involve the start is leg evaluations
        val lastScore = 0.0 // there is no length after the finish

        return listOf(firstScore) + legs.drop(1).dropLast(1).map {
            val best = it.best.distance
            when {
                best < minAllowed -> 1.0
                best > maxAllowed -> 1.0
                else -> 0.0
            }} + listOf(lastScore)
    }

}
