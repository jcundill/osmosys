package jon.test.constraints

import com.graphhopper.GHResponse
import jon.test.CourseParameters

class IsRouteableConstraint(val params: CourseParameters) : CourseConstraint {
    override fun valid(routedCourse: GHResponse) = !routedCourse.hasErrors()
}