package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class BeenThisWayBeforeScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * TODO: needs improvement - allocates the same to everything at the moment
     * works out how much of the leg to this numbered control has been travelled along
     * already
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> {

        val points = routedCourse.best.points.drop(1).dropLast(1)
        val recrossed = points - points.distinct()
        return routedLegs.dropLast(1).map { leg ->
            leg.best.points.intersect(recrossed).size.toDouble() / leg.best.points.size.toDouble()
        }
    }
}
