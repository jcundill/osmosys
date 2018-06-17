package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

class LegStraightLineScorer(val params: CourseParameters) : FeatureScorer  {
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> =
        // the finish can't be in the wrong place
        routedLegs.dropLast(1).map{ evaluate(it) }

    private fun evaluate(leg: GHResponse): Double {
        val first = leg.best.points.first()
        val last = leg.best.points.last()
        val crowFlies = params.dist2d.calcDist(first.lat, first.lon, last.lat, last.lon)
        val ratio = (leg.best.distance - crowFlies) / leg.best.distance

        return when {
            leg.best.distance == 0.0 -> 1.0
            else -> (1.0 - ratio) / 3.0
        }

    }

}