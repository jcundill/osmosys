package jon.test.constraints

import com.graphhopper.GHResponse

data class CourseLengthConstraint(val minAllowedDistance: Double, val maxAllowedDistance: Double) : CourseConstraint {

    /**
     * Here
     */
    override fun valid(routedCourse: GHResponse): Boolean {
        val distance = routedCourse.best.distance

        return distance in minAllowedDistance..maxAllowedDistance
    }


}