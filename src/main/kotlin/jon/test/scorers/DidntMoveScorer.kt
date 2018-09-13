package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.DistancePlaneProjection

class DidntMoveScorer : FeatureScorer {
    private val dist2d = DistancePlaneProjection()
    private val minMoveDistance: Double = 50.0

    /**
     * scores each numbered control based on the length of the previous leg.
     * i.e. control 2 is in a bad place as it in the same place as 1
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> =
            routedLegs.windowed(2, 1, false).map { ls ->
                if (isSameStartPoint(ls.first(), ls.last())) 1.0 else 0.0
            }

    private fun isSameStartPoint(first: GHResponse, second: GHResponse): Boolean {
        val xs = first.best.points
        val ys = second.best.points
        val dist = dist2d.calcDist(xs.getLat(0), xs.getLon(0), ys.getLat(0), ys.getLon(0))
        return dist < minMoveDistance
    }

}