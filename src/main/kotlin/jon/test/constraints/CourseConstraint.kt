package jon.test.constraints

import com.graphhopper.GHResponse

interface CourseConstraint {

    fun valid(routedCourse: GHResponse): Boolean
}