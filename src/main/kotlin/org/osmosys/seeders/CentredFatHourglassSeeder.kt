package org.osmosys.seeders

import org.osmosys.ControlSite
import org.osmosys.ControlSiteFinder
import kotlin.math.PI

class CentredFatHourglassSeeder(csf: ControlSiteFinder) : AbstractSeeder(csf) {

    override fun seed(initialPoints: List<ControlSite>, requestedNumControls: Int, requestedCourseLength: Double): List<ControlSite> {
        val longRatio = 1.414
        val shortRatio = 1.0 / longRatio
        val angle = 1.570796327

        val scaleFactor = requestedCourseLength * twistFactor / 4.5 //its a rectangleand a bit

        val first = initialPoints.first()
        val last = initialPoints.last()
        val bearing = csf.randomBearing
        val initial = csf.getCoords(first.position, bearing, scaleFactor * longRatio / 2.0)
        val second = csf.getCoords(initial, bearing + angle, scaleFactor * shortRatio / 2.0)
        val third = csf.getCoords(second, PI + bearing + angle, scaleFactor * shortRatio )
        val fourth =  csf.getCoords(third, PI + bearing, scaleFactor * longRatio)
        val fifth = csf.getCoords(fourth, bearing + angle, scaleFactor * longRatio)

        val points = listOf(first.position, second, fifth, fourth, third, last.position)
        return generateInitialCourse(points, requestedNumControls)
    }

}