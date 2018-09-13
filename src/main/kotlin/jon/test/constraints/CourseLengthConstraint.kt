package jon.test.constraints

import com.graphhopper.GHResponse

data class CourseLengthConstraint(val desiredDistance: Double) : CourseConstraint {

    private val allowedLengthDelta: Double = 0.2
    private val minAllowedDistance = desiredDistance - desiredDistance * allowedLengthDelta
    private val maxAllowedDistance = desiredDistance + desiredDistance * allowedLengthDelta
    /**
     * Here
     */
    override fun valid(routedCourse: GHResponse): Boolean =
            routedCourse.best.distance in minAllowedDistance..maxAllowedDistance


}