package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.DistancePlaneProjection
import jon.test.Params

class DidntMoveScorer(val params: Params) : FeatureScorer {
    val dist2d = DistancePlaneProjection()
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            listOf(0.0) + legs.windowed(2, 1, false).map { ls ->
                val xs = ls.first().best.points
                val ys = ls.last().best.points
                val dist = dist2d.calcDist(xs.getLat(0), xs.getLon(0), ys.getLat(0), ys.getLon(0))
                if (dist < 10) 1.0 else 0.0
            }

}