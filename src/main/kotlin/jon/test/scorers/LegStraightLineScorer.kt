package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper
import jon.test.improvers.dist2d

class LegStraightLineScorer : FeatureScorer {
    override fun score(routedLegs: List<GHResponse>, routedCourse: PathWrapper): List<Double> =
            routedLegs.dropLast(1).map { evaluate(it) }     // the finish can't be in the wrong place

    private fun evaluate(leg: GHResponse): Double {
        val first = leg.best.points.first()
        val last = leg.best.points.last()
        val crowFlies = dist2d.calcDist(first.lat, first.lon, last.lat, last.lon)
        val ratio = (leg.best.distance - crowFlies) / leg.best.distance

        return when {
            leg.best.distance == 0.0 -> 1.0
            else -> (1.0 - ratio) / 3.0
        }

    }

}