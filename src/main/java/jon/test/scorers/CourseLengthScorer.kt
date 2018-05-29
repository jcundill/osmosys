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
    previous leg.
    i.e. 2 is in a bad position when assessing overall length
    as the route from 1 to 2 was very long / very short
     */
    fun algo(legs: List<Double>, distance:Double): List<Double> {
        // the start cannot be in the wrong position - so set to zero
        // the finish cannot be in the wrong position so drop the route from the last control to the finish
        val contribs = listOf(0.0) + legs.dropLast(1).map { it / distance }
        val delta: (Double) -> Double  = when {
            distance < params.minAllowedDistance -> {a -> 1.0 - a}
            distance > params.maxAllowedDistance -> {a -> a}
            else -> { _ -> 0.0}
        }

        return contribs.map { delta(it) }.drop(1)
    }
}