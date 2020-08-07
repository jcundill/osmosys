package org.osmosys.seeders

import org.osmosys.ControlSite
import org.osmosys.ControlSiteFinder

class TriangleSeeder(csf: ControlSiteFinder) : AbstractSeeder(csf){

    override fun seed(initialPoints: List<ControlSite>, requestedNumControls: Int, requestedCourseLength: Double): List<ControlSite> {
        val circLength = 3.5
        val angle = 1.5658238

        val scaleFactor = requestedCourseLength * twistFactor / circLength

        val first = initialPoints.first()
        val last = initialPoints.last()
        val bearing = csf.randomBearing
        val second = csf.getCoords(first.position, Math.PI + bearing, scaleFactor)
        val third = csf.getCoords(last.position, Math.PI + bearing + angle, scaleFactor)

        val points = listOf(first.position, second, third, last.position)
        return generateInitialCourse(points, requestedNumControls)
    }

}