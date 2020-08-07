package org.osmosys.seeders

import org.osmosys.ControlSite
import org.osmosys.ControlSiteFinder

class HourglassSeeder(csf: ControlSiteFinder) : AbstractSeeder(csf) {

    override fun seed(initialPoints: List<ControlSite>, requestedNumControls: Int, requestedCourseLength: Double): List<ControlSite> {
        val longRatio = 1.414
        val shortRatio = 1.0 / longRatio
        val angle = 1.570796327

        val scaleFactor = requestedCourseLength * twistFactor / 4.5 //its a rectangle and a bit

        val first = initialPoints.first()
        val last = initialPoints.last()
        val bearing = csf.randomBearing
        val second = csf.getCoords(first.position, Math.PI + bearing, scaleFactor * longRatio)
        val third = csf.getCoords(second, Math.PI + bearing + angle, scaleFactor * shortRatio)
        val fourth =  csf.getCoords(third, bearing, scaleFactor * longRatio)

        val points = listOf(first.position, second, fourth, third, last.position)
        return generateInitialCourse(points, requestedNumControls)
    }

}