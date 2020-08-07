package org.osmosys.constraints

import com.graphhopper.GHResponse
import org.osmosys.ControlSite
import org.osmosys.improvers.dist

class MustVisitWaypointsConstraint(private val waypoints: List<ControlSite>) : CourseConstraint {
    override fun valid(routedCourse: GHResponse): Boolean {
        return when {
            waypoints.isEmpty() -> true
            else -> {
                waypoints.all { wpt -> getsCloseTo(wpt, routedCourse) }
            }
        }
    }

    private fun getsCloseTo(wpt: ControlSite, routedCourse: GHResponse): Boolean {
        return routedCourse.best.points.any { pt -> dist(pt, wpt.position) < 100.0 }
    }
}