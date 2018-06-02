package jon.test.constraints

import com.graphhopper.GHResponse
import jon.test.CourseParameters

data class CourseLengthConstraint(val params: CourseParameters) : CourseConstraint {

    /**
     * Here
     */
    override fun valid(routedCourse: GHResponse): Boolean {
        val distance = routedCourse.best.distance

        return distance >= params.minAllowedDistance && distance <= params.maxAllowedDistance
    }


}