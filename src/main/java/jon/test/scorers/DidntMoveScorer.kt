package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.DistancePlaneProjection
import jon.test.CourseParameters

data class DidntMoveScorer(val params: CourseParameters) : FeatureScorer {
    private val dist2d = DistancePlaneProjection()
    private val minAllowed = 5

    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> =
            legs.windowed(2, 1, false).map { ls ->
                if (isSameStartPoint(ls.first(), ls.last())) 1.0 else 0.0
            } + 0.0

    private fun isSameStartPoint(first: GHResponse, second: GHResponse): Boolean {
        val xs = first.best.points
        val ys = second.best.points
        val dist = dist2d.calcDist(xs.getLat(0), xs.getLon(0), ys.getLat(0), ys.getLon(0))
        return dist < params.minControlSeparation
    }

}