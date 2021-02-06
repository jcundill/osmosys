package org.osmosys.constraints

import com.graphhopper.GHResponse
import org.osmosys.improvers.dist

class DidntMoveConstraint : CourseConstraint {
    private val minMoveDistance: Double = 50.0

    override fun valid(routedCourse: GHResponse): Boolean {
        val controls = routedCourse.best.waypoints
        return controls.windowed(2).all{ dist(it.first(), it.last()) > minMoveDistance}
    }
}