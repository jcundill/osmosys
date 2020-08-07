package org.osmosys.seeders

import com.graphhopper.util.shapes.GHPoint
import org.osmosys.ControlSite
import org.osmosys.ControlSiteFinder

abstract class AbstractSeeder(protected val csf: ControlSiteFinder) : SeedingStrategy {
    val twistFactor = 0.67 //probable crow files disance vs actual distance

    abstract override fun seed(initialPoints: List<ControlSite>, requestedNumControls: Int, requestedCourseLength: Double): List<ControlSite>

    private fun fillFromInitialPoints(points: List<ControlSite>, requestedNumControls: Int): List<ControlSite> {
        val pointList = csf.routeRequest(points).best.points
        return (1..requestedNumControls).mapNotNull {
            val position = pointList.get(it * (pointList.size / requestedNumControls - 1))
            csf.findNearestControlSiteTo(position)
        }
    }

    protected fun generateInitialCourse(route: List<GHPoint>, requestedNumControls: Int): List<ControlSite> {
        val points = route.map { csf.findControlSiteNear(it, 50.0) }
        return fillFromInitialPoints(points, requestedNumControls)
    }
}