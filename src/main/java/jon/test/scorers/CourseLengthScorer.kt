package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class CourseLengthScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * Here
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> {
        val distance = routedCourse.best.distance
        val components = routedLegs.map { it.best.distance }

        return algo(components, distance)
    }

    /*
    the value assigned to the control is the contribution of the
    previous leg and the next leg.
    i.e. 2 is in a bad position when assessing overall length
    as the route from 1 to 2 was very long / very short aswas the route from 2 to 3
     */
    fun algo(legs: List<Double>, distance:Double): List<Double> {
        // the start cannot be in the wrong position - so set to zero
        // the finish cannot be in the wrong position so drop the route from the last control to the finish
        val legContribs = legs.map { it / distance }
        val numberedControlContribs = legContribs.windowed(2).map {it[0] + it[1]}
        val delta: (Double) -> Double  = when {
            distance < params.minAllowedDistance -> {a -> 1.0 - a}
            distance > params.maxAllowedDistance -> {a -> a}
            else -> { _ -> 0.0}
        }

        return numberedControlContribs.map { delta(it) }
    }
}