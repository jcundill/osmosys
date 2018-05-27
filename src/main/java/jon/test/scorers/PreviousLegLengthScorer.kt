package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.Params

data class PreviousLegLengthScorer(val params: Params): FeatureScorer{

    private val minAllowed = 20.0
    private val maxAllowed = params.distance / 3.0  // third of the course on a single leg

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        val firstScore = 0.0 // there is no length to the start

        return listOf(firstScore) + legs.dropLast(2).map {
            val best = it.best.distance
            when {
                best < minAllowed -> 1.0
                best > maxAllowed -> 1.0
                else -> 0.0
            }} + 0.0
    }

}
