package org.osmosys.constraints

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.BBox

class FitsExistingMapConstraint(private val mapBox: BBox) : CourseConstraint {
    override fun valid(routedCourse: GHResponse): Boolean {
        return routedCourse.best.points.all { mapBox.contains(it.lat, it.lon) }
    }

}
