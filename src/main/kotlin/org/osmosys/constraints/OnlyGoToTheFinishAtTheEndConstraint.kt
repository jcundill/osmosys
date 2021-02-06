package org.osmosys.constraints

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import org.osmosys.improvers.dist

class OnlyGoToTheFinishAtTheEndConstraint : CourseConstraint {
    override fun valid(routedCourse: GHResponse): Boolean {
        val track = routedCourse.best.points
        val finish = track.last()
        val trackAfterApproachingFinish = track.dropWhile {  dist(it, finish) <= 150.0 }.dropWhile { dist(it, finish) > 150.0 }
        val allgood= trackAfterApproachingFinish.all { dist(it, finish) < 150.0}
        return allgood
    }

}