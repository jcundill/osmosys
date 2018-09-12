package jon.test.constraints

import com.graphhopper.GHResponse

class IsRouteableConstraint : CourseConstraint {
    override fun valid(routedCourse: GHResponse) = !routedCourse.hasErrors()
}