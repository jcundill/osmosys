package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.Params

class LegComplexityScorer(params: Params) : FeatureScorer {

    private val minTurns = 6

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.map{
                val turns = it.best.instructions.size
                when {
                    turns > minTurns -> 0.0
                    else -> (minTurns - turns)/ minTurns.toDouble()
                }
            }

}
