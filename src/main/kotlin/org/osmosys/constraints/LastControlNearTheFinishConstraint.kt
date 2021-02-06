package org.osmosys.constraints

import com.graphhopper.GHResponse
import org.osmosys.improvers.dist

class LastControlNearTheFinishConstraint : CourseConstraint {
    override fun valid(routedCourse: GHResponse): Boolean {
        val controls = routedCourse.best.waypoints
        val num = controls.size - 1
        return dist(controls[num], controls[num - 1])  <= 500.0
    }
}